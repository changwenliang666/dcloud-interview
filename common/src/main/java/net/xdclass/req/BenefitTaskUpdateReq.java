package net.xdclass.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 权益任务状态更新请求
 * 用于ai-service调用更新task状态
 *
 * @author 小滴课堂
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BenefitTaskUpdateReq", description = "权益任务状态更新请求")
public class BenefitTaskUpdateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID（任务表主键）")
    private String messageId;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "业务类型：RESUME_ANALYSE-简历分析, INTERVIEW_CREATE-面试创建")
    private String businessType;

    @Schema(description = "操作类型：FINISH-完成, CANCEL-取消回滚")
    private String operation;

    @Schema(description = "失败原因（取消时填写）")
    private String failReason;
}