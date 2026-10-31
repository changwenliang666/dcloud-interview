package net.xdclass.service;

import net.xdclass.model.BenefitTaskDO;
import net.xdclass.req.BenefitTaskUpdateReq;
public interface BenefitTaskService {
    void checkAndCompensate(String messageId, Integer checkLevel);

    /**
     * 延迟检查重试耗尽后结案。能读到简历状态就按状态结案，读不到则退次。
     */
    void compensateWhenStatusUnknown(String messageId);

    boolean updateTaskStatus(BenefitTaskUpdateReq req,BenefitTaskDO benefitTaskDO);

    BenefitTaskDO saveTask(Long businessId, String benefitCode, Long accountId, Long id, int count);
}
