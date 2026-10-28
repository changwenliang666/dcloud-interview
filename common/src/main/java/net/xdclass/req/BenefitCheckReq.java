package net.xdclass.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权益检查和扣减请求
 */
@Data
@Schema(name = "BenefitCheckReq", description = "权益检查和扣减请求")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenefitCheckReq {

    @Schema(description = "权益编码", required = true)
    private String benefitCode;

    @Schema(description = "账号", required = true)
    private Long accountId;

    @Schema(description = "简历ID 或者 面试ID", required = false)
    private Long businessId;

    @Schema(description = "次数", required = false)
    private Integer count;

}