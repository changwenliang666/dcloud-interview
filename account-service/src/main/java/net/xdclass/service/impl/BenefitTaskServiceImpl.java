package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.BenefitDelayCheckMQConfig;
import net.xdclass.constant.ResumeAnalyseCheckLevel;
import net.xdclass.dto.BenefitDelayCheckMessageDTO;
import net.xdclass.dto.ResumeCheckStatusDTO;
import net.xdclass.enums.BenefitEnum;
import net.xdclass.enums.BenefitTaskStateEnum;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.ResumeStatus;
import net.xdclass.exception.BizException;
import net.xdclass.feign.AiBusinessServiceStatusFegin;
import net.xdclass.mapper.AccountBenefitMapper;
import net.xdclass.mapper.BenefitTaskMapper;
import net.xdclass.model.AccountBenefitDO;
import net.xdclass.model.BenefitTaskDO;
import net.xdclass.req.BenefitTaskUpdateReq;
import net.xdclass.service.BenefitTaskService;
import net.xdclass.util.JsonData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BenefitTaskServiceImpl implements BenefitTaskService {

    private final BenefitTaskMapper benefitTaskMapper;

    private final AccountBenefitMapper accountBenefitMapper;

    private final AiBusinessServiceStatusFegin aiBusinessServiceStatusFegin;

    private final RabbitTemplate rabbitTemplate;

    private BenefitTaskService self;

    @Autowired
    public void setSelf(@Lazy BenefitTaskService self) {
        this.self = self;
    }

    @Override
    public void checkAndCompensate(String messageId, Integer checkLevel) {
        BenefitTaskDO benefitTaskDO = findByMessageId(messageId);
        if (benefitTaskDO == null) {
            log.error("消息id对应的记录不存在, messageId:{}", messageId);
            return;
        }

        BenefitTaskStateEnum currentStatus = BenefitTaskStateEnum.valueOf(benefitTaskDO.getLockState());
        log.info("触发第{}次检查, messageId:{}, status:{}", checkLevel, messageId, currentStatus);
        if (currentStatus.isFinish() || currentStatus.isCancel()) {
            log.info("任务已结束，无需补偿, messageId:{}, status:{}", messageId, currentStatus);
            return;
        }
        if (!BenefitEnum.RESUME_ANALYSE.name().equals(benefitTaskDO.getBenefitCode())) {
            log.info("当前权益不走简历延迟结案, messageId:{}, benefitCode:{}", messageId, benefitTaskDO.getBenefitCode());
            return;
        }
        if (Objects.equals(checkLevel, ResumeAnalyseCheckLevel.REDISPATCH)) {
            ensureAbsoluteDeadline(benefitTaskDO);
        }

        boolean forceTimeout = ResumeAnalyseCheckLevel.isForceTimeout(checkLevel);
        ResumeCheckStatusDTO resumeStatus = loadResumeStatus(benefitTaskDO, checkLevel);
        if (resumeStatus == null || resumeStatus.getStatus() == null) {
            if (!forceTimeout) {
                log.warn("查询简历状态失败，等待下次检查, messageId:{}, checkLevel:{}", messageId, checkLevel);
                return;
            }
            log.error("最终检查查询简历状态失败, messageId:{}", messageId);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }

        BenefitTaskStateEnum decision = decide(messageId, resumeStatus.getStatus(), checkLevel);
        if (decision == null) {
            log.info("简历分析中，等待下次检查, messageId:{}, checkLevel:{}, resumeStatus:{}",
                    messageId, checkLevel, resumeStatus);
            return;
        }
        if (!settle(benefitTaskDO, decision)) {
            log.info("结案未生效，任务状态已变化, messageId:{}, operation:{}", messageId, decision);
        }
    }

    @Override
    public void compensateWhenStatusUnknown(String messageId) {
        BenefitTaskDO benefitTaskDO = findByMessageId(messageId);
        if (benefitTaskDO == null) {
            log.error("重试耗尽时任务不存在, messageId:{}", messageId);
            return;
        }
        BenefitTaskStateEnum currentStatus = BenefitTaskStateEnum.valueOf(benefitTaskDO.getLockState());
        if (currentStatus.isFinish() || currentStatus.isCancel()) {
            log.info("重试耗尽时任务已结束, messageId:{}, status:{}", messageId, currentStatus);
            return;
        }
        try {
            checkAndCompensate(messageId, ResumeAnalyseCheckLevel.ABSOLUTE_DEADLINE);
            BenefitTaskDO latest = findByMessageId(messageId);
            if (latest != null && BenefitTaskStateEnum.LOCK.name().equals(latest.getLockState())) {
                log.error("强制超时后任务仍未结案，直接退次, messageId:{}", messageId);
                self.updateTaskStatus(BenefitTaskUpdateReq.builder()
                        .messageId(messageId)
                        .operation(BenefitTaskStateEnum.CANCEL.getCode())
                        .build(), latest);
            }
            return;
        } catch (Exception e) {
            log.error("重试耗尽后强制检查失败，直接退次, messageId:{}", messageId, e);
        }
        self.updateTaskStatus(BenefitTaskUpdateReq.builder()
                .messageId(messageId)
                .operation(BenefitTaskStateEnum.CANCEL.getCode())
                .build(), benefitTaskDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public boolean updateTaskStatus(BenefitTaskUpdateReq req, BenefitTaskDO benefitTaskDO) {
        if (benefitTaskDO == null) {
            benefitTaskDO = findByMessageId(req.getMessageId());
        }
        if (benefitTaskDO == null) {
            log.info("消息id对应的记录不存在 {}", req.getMessageId());
            return false;
        }

        BenefitTaskStateEnum currentStatus = BenefitTaskStateEnum.valueOf(benefitTaskDO.getLockState());
        log.info("任务当前状态, messageId:{}, status:{}", req.getMessageId(), currentStatus);
        if (currentStatus.isFinish() || currentStatus.isCancel()) {
            boolean accepted = currentStatus.getCode().equals(req.getOperation());
            log.info("任务已结束, messageId:{}, status:{}, operation:{}, accepted:{}",
                    req.getMessageId(), currentStatus, req.getOperation(), accepted);
            return accepted;
        }

        if (BenefitTaskStateEnum.FINISH.getCode().equals(req.getOperation())) {
            return markTaskState(benefitTaskDO, BenefitTaskStateEnum.FINISH)
                    || isCurrentState(benefitTaskDO, BenefitTaskStateEnum.FINISH);
        }
        if (BenefitTaskStateEnum.CANCEL.getCode().equals(req.getOperation())) {
            if (!markTaskState(benefitTaskDO, BenefitTaskStateEnum.CANCEL)) {
                return isCurrentState(benefitTaskDO, BenefitTaskStateEnum.CANCEL);
            }
            restoreBenefit(benefitTaskDO);
            return true;
        }

        log.info("无效的操作, operation:{}, messageId:{}", req.getOperation(), req.getMessageId());
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BenefitTaskDO saveTask(Long businessId, String benefitCode, Long accountId, Long accountBenefitId, int count) {
        String messageId = UUID.randomUUID().toString().replace("-", "");
        BenefitTaskDO taskDO = BenefitTaskDO.builder()
                .messageId(messageId)
                .businessId(businessId)
                .benefitCode(benefitCode)
                .accountId(accountId)
                .accountBenefitId(accountBenefitId)
                .useTimes(count)
                .lockState(BenefitTaskStateEnum.LOCK.name())
                .build();

        benefitTaskMapper.insert(taskDO);
        log.info("保存task记录成功, messageId:{}", messageId);
        return taskDO;
    }

    /**
     * 仅当当前仍是 LOCK 时才更新，避免并发下重复完成或重复取消。
     *
     * @return 是否由本次调用完成状态迁移
     */
    private boolean markTaskState(BenefitTaskDO benefitTaskDO, BenefitTaskStateEnum targetState) {
        int rows = benefitTaskMapper.update(null, new LambdaUpdateWrapper<BenefitTaskDO>()
                .set(BenefitTaskDO::getLockState, targetState.name())
                .eq(BenefitTaskDO::getId, benefitTaskDO.getId())
                .eq(BenefitTaskDO::getLockState, BenefitTaskStateEnum.LOCK.name()));
        if (rows != 1) {
            log.info("任务状态已变化，跳过更新, messageId:{}, target:{}", benefitTaskDO.getMessageId(), targetState);
            return false;
        }
        log.info("任务状态更新成功, messageId:{}, target:{}", benefitTaskDO.getMessageId(), targetState);
        return true;
    }

    private boolean isCurrentState(BenefitTaskDO benefitTaskDO, BenefitTaskStateEnum expected) {
        BenefitTaskDO latest = benefitTaskMapper.selectById(benefitTaskDO.getId());
        return latest != null && expected.name().equals(latest.getLockState());
    }

    private void restoreBenefit(BenefitTaskDO benefitTaskDO) {
        Integer useTimes = benefitTaskDO.getUseTimes();
        if (useTimes == null || useTimes <= 0) {
            log.error("补偿次数无效, messageId:{}, useTimes:{}", benefitTaskDO.getMessageId(), useTimes);
            throw new BizException(BizCodeEnum.BENEFIT_NOT_ENOUGH);
        }

        int rows = accountBenefitMapper.update(null, new LambdaUpdateWrapper<AccountBenefitDO>()
                .setSql("remaining_count = remaining_count + {0}", useTimes)
                .eq(AccountBenefitDO::getId, benefitTaskDO.getAccountBenefitId()));
        if (rows != 1) {
            log.error("恢复权益失败, messageId:{}, accountBenefitId:{}, useTimes:{}",
                    benefitTaskDO.getMessageId(), benefitTaskDO.getAccountBenefitId(), useTimes);
            throw new BizException(BizCodeEnum.BENEFIT_NOT_EXIST);
        }
        log.info("恢复权益成功, messageId:{}, accountBenefitId:{}, useTimes:{}",
                benefitTaskDO.getMessageId(), benefitTaskDO.getAccountBenefitId(), useTimes);
    }

    private void ensureAbsoluteDeadline(BenefitTaskDO task) {
        try {
            BenefitDelayCheckMessageDTO messageDTO = BenefitDelayCheckMessageDTO.builder()
                    .messageId(task.getMessageId())
                    .businessId(task.getBusinessId())
                    .checkLevel(ResumeAnalyseCheckLevel.ABSOLUTE_DEADLINE)
                    .build();
            rabbitTemplate.convertAndSend(BenefitDelayCheckMQConfig.BENEFIT_DELAY_CHECK_EXCHANGE,
                    BenefitDelayCheckMQConfig.DELAY_CHECK_ROUTING_KEY_20MIN, messageDTO);
            log.info("补发绝对超时检查, messageId={}", task.getMessageId());
        } catch (Exception e) {
            log.error("补发绝对超时检查失败, messageId={}", task.getMessageId(), e);
        }
    }

    private ResumeCheckStatusDTO loadResumeStatus(BenefitTaskDO task, Integer checkLevel) {
        try {
            JsonData result = aiBusinessServiceStatusFegin.checkStatus(task.getBusinessId(), task.getAccountId(),
                    task.getMessageId(), checkLevel);
            if (result == null) {
                log.error("查询简历状态无响应, messageId:{}", task.getMessageId());
                return null;
            }
            if (Objects.equals(result.getCode(), BizCodeEnum.RESUME_NOT_EXIST.getCode())) {
                log.warn("简历不存在，按失败结案, messageId:{}, businessId:{}", task.getMessageId(), task.getBusinessId());
                return ResumeCheckStatusDTO.builder()
                        .status(ResumeStatus.ANALYZE_TIMEOUT.name())
                        .dispatched(false)
                        .build();
            }
            if (!Objects.equals(result.getCode(), 0) || result.getData() == null) {
                log.warn("查询简历状态失败, messageId:{}, code:{}, msg:{}", task.getMessageId(), result.getCode(), result.getMsg());
                return null;
            }
            return result.getData(ResumeCheckStatusDTO.class);
        } catch (Exception e) {
            log.error("查询简历状态异常, messageId:{}, checkLevel:{}", task.getMessageId(), checkLevel, e);
            return null;
        }
    }

    private BenefitTaskStateEnum decide(String messageId, String resumeStatus, Integer checkLevel) {
        if (ResumeStatus.ANALYZED.name().equals(resumeStatus)) {
            return BenefitTaskStateEnum.FINISH;
        }
        if (ResumeStatus.isProcessing(resumeStatus)) {
            if (!ResumeAnalyseCheckLevel.isForceTimeout(checkLevel)) {
                return null;
            }
            log.error("强制超时检查时简历仍在分析中, messageId:{}, resumeStatus:{}", messageId, resumeStatus);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
        if (ResumeStatus.isResumeAnalyzeError(resumeStatus)) {
            return BenefitTaskStateEnum.CANCEL;
        }
        log.warn("未知简历状态, messageId:{}, resumeStatus:{}", messageId, resumeStatus);
        return ResumeAnalyseCheckLevel.isForceTimeout(checkLevel) ? BenefitTaskStateEnum.CANCEL : null;
    }

    private boolean settle(BenefitTaskDO task, BenefitTaskStateEnum target) {
        return self.updateTaskStatus(BenefitTaskUpdateReq.builder()
                .messageId(task.getMessageId())
                .operation(target.getCode())
                .build(), task);
    }

    private BenefitTaskDO findByMessageId(String messageId) {
        return benefitTaskMapper.selectOne(new LambdaQueryWrapper<BenefitTaskDO>()
                .eq(BenefitTaskDO::getMessageId, messageId));
    }
}
