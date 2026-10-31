package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.BenefitDelayCheckMQConfig;
import net.xdclass.constant.ResumeAnalyseCheckLevel;
import net.xdclass.dto.BenefitCheckResultDTO;
import net.xdclass.dto.BenefitDelayCheckMessageDTO;
import net.xdclass.dto.BenefitGrantMessage;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.exception.BizException;
import net.xdclass.mapper.AccountBenefitMapper;
import net.xdclass.model.AccountBenefitDO;
import net.xdclass.model.BenefitTaskDO;
import net.xdclass.req.BenefitCheckReq;
import net.xdclass.service.AccountBenefitService;
import net.xdclass.service.BenefitTaskService;
import net.xdclass.util.JsonData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountBenefitServiceImpl implements AccountBenefitService {

    private final AccountBenefitMapper accountBenefitMapper;
    private final BenefitTaskService benefitTaskService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantBenefit(BenefitGrantMessage message) {
        List<BenefitGrantMessage.BenefitItem> benefitItems = message.getBenefitItems();
        if(benefitItems == null ||  benefitItems.isEmpty()) {
            log.info("权益为空，跳过发放，订单编号:{}",message.getOutTradeNo());
            return;
        }

        for(BenefitGrantMessage.BenefitItem benefitItem : benefitItems) {
            grantSingleItem(message, benefitItem);
        }


    }

    /**
     * 发放单条权益：每次购买均插入新记录，保持独立有效期
     */
    private void grantSingleItem(BenefitGrantMessage message, BenefitGrantMessage.BenefitItem item) {
        String benefitCode = item.getBenefitCode();
        int count = item.getCount();

        // 幂等检查：同一订单同一权益ID只发放一次（防止 MQ 重复消费）
        Long existByOrder = accountBenefitMapper.selectCount(
                new LambdaQueryWrapper<AccountBenefitDO>()
                        .eq(AccountBenefitDO::getProductOrderId, message.getOrderId())
                        .eq(AccountBenefitDO::getBenefitId, item.getBenefitId())
        );
        if (existByOrder > 0) {
            log.warn("权益已发放，跳过（幂等），orderId:{}, benefitId:{}", message.getOrderId(), item.getBenefitId());
            return;
        }

        // 每次购买插入新记录，保持独立有效期
        Date startTime = new Date();
        Date endTime = calcEndTime(startTime, item.getValidDays(), count);

        AccountBenefitDO accountBenefitDO = AccountBenefitDO.builder()
                .accountId(message.getAccountId())
                .benefitId(item.getBenefitId())
                .benefitCode(benefitCode)
                .productOrderId(message.getOrderId())
                .orderType(message.getOrderType())
                .remainingCount(count)
                .totalCount(count)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        accountBenefitMapper.insert(accountBenefitDO);
        log.info("权益发放成功, accountId:{}, benefitId:{}, benefitCode:{}, orderType:{}, count:{}, startTime:{}, endTime:{}",
                message.getAccountId(), item.getBenefitId(), benefitCode, message.getOrderType(), count, startTime, endTime);
    }

    /**
     * 计算权益到期时间
     *
     * @param baseTime  计算起点
     * @param validDays 单次有效天数（null 时默认 365 天）
     * @param count     本次发放次数
     */
    private Date calcEndTime(Date baseTime, Integer validDays, int count) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseTime);
        int days = (validDays != null && validDays > 0) ? validDays * count : 365 * count;
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }


    /**
     * 检查并扣减权益
     *
     * 设计说明：
     * - 权益扣减和任务保存在同一事务中，保证数据一致性
     * - 返回messageId供ai-service发送业务MQ使用
     *
     * 事务回滚 → MQ消息已发送 → 消费时查不到任务记录 → 跳过处理 ✓
     * 事务提交 → MQ消息已发送 → 消费时查到任务记录 → 正常处理 ✓
     * @param req 权益检查请求
     * @return 扣减结果（包含messageId）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JsonData checkAndDeductBenefit(BenefitCheckReq req) {
        // 本次要扣减的权益数量
        int deductedCount = req.getCount() != null ? req.getCount() : 1;
        // 1. 查询可用权益（优先使用即将过期的）
        List<AccountBenefitDO> benefitRecords = accountBenefitMapper.selectList(
                new LambdaQueryWrapper<AccountBenefitDO>()
                        .eq(AccountBenefitDO::getAccountId, req.getAccountId())
                        .eq(AccountBenefitDO::getBenefitCode, req.getBenefitCode())
                        .gt(AccountBenefitDO::getEndTime, new Date())
                        .ge(AccountBenefitDO::getRemainingCount, deductedCount)
                        .orderByAsc(AccountBenefitDO::getEndTime)
        );
        if (benefitRecords.isEmpty()) {
            log.warn("权益不足, accountId={}, benefitCode={},deductedCount:{}", req.getAccountId(), req.getBenefitCode(),deductedCount);
            return JsonData.buildResult(BizCodeEnum.BENEFIT_NOT_ENOUGH);
        }

        // 2. 创建权益任务记录（本地消息表，用于补偿机制）
        AccountBenefitDO accountBenefitDO = benefitRecords.getFirst();

        BenefitTaskDO task = benefitTaskService.saveTask(
                req.getBusinessId(),
                req.getBenefitCode(),
                req.getAccountId(),
                accountBenefitDO.getId(),
                deductedCount
        );

        int rows = accountBenefitMapper.update(new LambdaUpdateWrapper<AccountBenefitDO>()
                .setSql("remaining_count = remaining_count - {0}", deductedCount)
                .eq(AccountBenefitDO::getId, accountBenefitDO.getId())
                .ge(AccountBenefitDO::getRemainingCount, deductedCount));
        if (rows != 1) {
            log.warn("权益扣减冲突或余量不足, accountBenefitId={}, deductedCount={}", accountBenefitDO.getId(), deductedCount);
            throw new BizException(BizCodeEnum.BENEFIT_NOT_ENOUGH);
        }
        // 重新查询剩余的库存数量
        AccountBenefitDO latestBenefit = accountBenefitMapper.selectById(accountBenefitDO.getId());
        accountBenefitDO.setRemainingCount(latestBenefit.getRemainingCount());

        sendDelayCheckMessages(task.getMessageId(), req.getBusinessId());
        log.info("权益扣减成功, accountBenefitId={}, messageId={}, businessId={}, benefitCode={}",
                accountBenefitDO.getId(), task.getMessageId(), req.getBusinessId(), req.getBenefitCode());


        // 5. 构建返回结果（供ai-service使用）
        return JsonData.buildSuccess(BenefitCheckResultDTO.builder()
                .success(true)
                .messageId(task.getMessageId())
                .businessId(req.getBusinessId())
                .benefitCode(req.getBenefitCode())
                .accountBenefitId(accountBenefitDO.getId())
                .remainingCount(accountBenefitDO.getRemainingCount())
                .deductedCount(deductedCount)
                .build());
    }

    @Override
    public void scheduleAnalyseDeadline(String messageId, Long businessId) {
        sendDelayCheck(messageId, businessId, ResumeAnalyseCheckLevel.INVOKE_DEADLINE,
                BenefitDelayCheckMQConfig.DELAY_CHECK_ROUTING_KEY_8MIN);
        log.info("已登记Serverless接收后的超时检查, messageId={}", messageId);
    }

    private void sendDelayCheckMessages(String messageId, Long businessId) {
        sendDelayCheck(messageId, businessId, ResumeAnalyseCheckLevel.PROBE,
                BenefitDelayCheckMQConfig.DELAY_CHECK_ROUTING_KEY_1MIN);
        sendDelayCheck(messageId, businessId, ResumeAnalyseCheckLevel.REDISPATCH,
                BenefitDelayCheckMQConfig.DELAY_CHECK_ROUTING_KEY_5MIN);
        sendDelayCheck(messageId, businessId, ResumeAnalyseCheckLevel.ABSOLUTE_DEADLINE,
                BenefitDelayCheckMQConfig.DELAY_CHECK_ROUTING_KEY_20MIN);
        log.info("发送权益延迟检查消息, messageId={}", messageId);
    }

    private void sendDelayCheck(String messageId, Long businessId, int checkLevel, String routingKey) {
        BenefitDelayCheckMessageDTO messageDTO = BenefitDelayCheckMessageDTO.builder()
                .messageId(messageId)
                .businessId(businessId)
                .checkLevel(checkLevel)
                .build();
        rabbitTemplate.convertAndSend(BenefitDelayCheckMQConfig.BENEFIT_DELAY_CHECK_EXCHANGE, routingKey, messageDTO);
    }
}
