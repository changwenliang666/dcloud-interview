package net.xdclass.feign;

import io.swagger.v3.oas.annotations.Operation;
import net.xdclass.req.BenefitCheckReq;
import net.xdclass.req.BenefitTaskUpdateReq;
import net.xdclass.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 账户权益服务Feign客户端
 *
 * 设计说明：
 * ai-service调用account-service扣减权益
 * account-service只负责权益扣减和延迟检查
 * ai-service负责发送业务MQ消息
 */
@FeignClient(name = "account-service", contextId = "AccountBenefitService")
public interface AccountBenefitFeign {

    /**
     * 检查并扣减用户权益
     *
     * @param benefitCheckReq 权益检查请求
     * @return 扣减结果（包含messageId，用于发送业务MQ）
     */
    @Operation(summary = "检查并扣减用户权益", description = "扣减权益并返回messageId，用于后续发送业务MQ")
    @PostMapping("/api/v1/account/benefit/checkAndDeduct")
    JsonData checkAndDeductBenefit(@RequestBody BenefitCheckReq benefitCheckReq);

    /**
     * 更新任务状态
     * 支持完成和取消回滚两种操作
     *
     * @param req 状态更新请求
     * @return 更新结果
     */
    @Operation(summary = "更新任务状态", description = "更新权益任务状态，支持完成和取消回滚")
    @PostMapping("/api/v1/account/benefit/task/updateStatus")
    JsonData updateTaskStatus(@RequestBody BenefitTaskUpdateReq req);

    /**
     * Serverless 已接收后，登记 8 分钟强制超时检查。
     */
    @PostMapping("/api/v1/account/benefit/scheduleAnalyseDeadline")
    JsonData scheduleAnalyseDeadline(@RequestParam("messageId") String messageId,
                                     @RequestParam("businessId") Long businessId);
}