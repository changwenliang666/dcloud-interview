package net.xdclass.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.enums.BenefitTaskStateEnum;
import net.xdclass.feign.AccountBenefitFeign;
import net.xdclass.req.BenefitTaskUpdateReq;
import net.xdclass.util.JsonData;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 分析结束后回调 account-service 结案。
 * 网络异常不在这里退次或扣次，简历状态留给延迟检查。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BenefitTaskCallback {

    public enum Result {
        /** 结案已确认 */
        CONFIRMED,
        /** 任务已是另一终态 */
        REJECTED,
        /** 调用 account-service 异常 */
        CALL_FAILED
    }

    private final AccountBenefitFeign accountBenefitFeign;

    public Result notify(String messageId, BenefitTaskStateEnum operation) {
        try {
            JsonData result = accountBenefitFeign.updateTaskStatus(BenefitTaskUpdateReq.builder()
                    .messageId(messageId)
                    .operation(operation.getCode())
                    .build());
            if (result != null && Objects.equals(result.getCode(), 0)) {
                log.info("权益任务回调成功, messageId:{}, operation:{}", messageId, operation);
                return Result.CONFIRMED;
            }
            log.warn("权益任务回调被拒绝, messageId:{}, operation:{}, code:{}, msg:{}",
                    messageId, operation,
                    result == null ? null : result.getCode(),
                    result == null ? null : result.getMsg());
            return Result.REJECTED;
        } catch (Exception e) {
            log.error("调用account-service更新权益任务失败, messageId:{}, operation:{}", messageId, operation, e);
            return Result.CALL_FAILED;
        }
    }
}
