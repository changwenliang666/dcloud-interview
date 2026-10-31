package net.xdclass.service;

import net.xdclass.dto.BenefitGrantMessage;
import net.xdclass.req.BenefitCheckReq;
import net.xdclass.util.JsonData;

public interface AccountBenefitService {
    void grantBenefit(BenefitGrantMessage message);
    JsonData checkAndDeductBenefit(BenefitCheckReq req);

    /**
     * Serverless 已接收分析请求后，登记 8 分钟后的强制超时检查。
     */
    void scheduleAnalyseDeadline(String messageId, Long businessId);
}
