package net.xdclass.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权益检查和扣减结果DTO
 * 用于account-service返回给ai-service的扣减结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BenefitCheckResultDTO", description = "权益检查和扣减结果")
public class BenefitCheckResultDTO {

    @Schema(description = "是否扣减成功")
    private Boolean success;

    @Schema(description = "消息ID（任务表主键，用于发送MQ消息和延迟检查）")
    private String messageId;

    @Schema(description = "业务ID")
    private Long businessId;

    @Schema(description = "权益编码")
    private String benefitCode;

    @Schema(description = "用户权益记录ID")
    private Long accountBenefitId;

    @Schema(description = "剩余次数")
    private Integer remainingCount;

    @Schema(description = "扣减次数")
    private Integer deductedCount;

    @Schema(description = "错误信息")
    private String errorMessage;
}

