package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.dto.BenefitGrantMessage;
import net.xdclass.mapper.AccountBenefitMapper;
import net.xdclass.model.AccountBenefitDO;
import net.xdclass.service.AccountBenefitService;
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
}
