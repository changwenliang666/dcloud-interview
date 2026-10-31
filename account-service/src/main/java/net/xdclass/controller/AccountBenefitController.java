package net.xdclass.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.req.BenefitCheckReq;
import net.xdclass.req.BenefitTaskUpdateReq;
import net.xdclass.service.AccountBenefitService;
import net.xdclass.service.BenefitTaskService;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户权益管理控制器
 */
@Tag(name = "用户权益", description = "个人中心权益管理接口")
@RestController
@RequestMapping("/api/v1/account/benefit")
@Slf4j
public class AccountBenefitController {

    @Autowired
    private AccountBenefitService accountBenefitService;

    @Autowired
    private BenefitTaskService benefitTaskService;


    /**
     * 检查并扣减用户权益
     *
     * 设计说明（方案1优化）：
     * - 只负责权益扣减和延迟检查
     * - 返回messageId供ai-service发送业务MQ
     *
     * @return 扣减结果
     */
    @Operation(summary = "检查并扣减用户权益", description = "扣减权益并返回messageId，用于后续发送业务MQ")
    @PostMapping("/checkAndDeduct")
    JsonData checkAndDeduct(@RequestBody BenefitCheckReq benefitCheckReq){
        return accountBenefitService.checkAndDeductBenefit(benefitCheckReq);
    }

    /**
     * 更新任务状态（由ai-service调用）
     * 支持完成和取消回滚两种操作
     *
     * @param req 状态更新请求
     * @return 更新结果
     */
    @Operation(summary = "更新任务状态", description = "更新权益任务状态，支持完成和取消回滚")
    @PostMapping("/task/updateStatus")
    JsonData updateTaskStatus(@RequestBody BenefitTaskUpdateReq req) {
        boolean success = benefitTaskService.updateTaskStatus(req,null);
        if (success) {
            return JsonData.buildSuccess();
        } else {
            return JsonData.buildError("任务状态更新失败");
        }
    }

    @Operation(summary = "登记分析接收后的超时检查", description = "Serverless 返回已接收后，8分钟后强制检查简历是否仍在分析中")
    @PostMapping("/scheduleAnalyseDeadline")
    JsonData scheduleAnalyseDeadline(@RequestParam("messageId") String messageId,
                                     @RequestParam("businessId") Long businessId) {
        accountBenefitService.scheduleAnalyseDeadline(messageId, businessId);
        return JsonData.buildSuccess();
    }

}