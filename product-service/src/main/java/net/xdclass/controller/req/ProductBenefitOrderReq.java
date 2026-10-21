package net.xdclass.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(name = "ProductBenefitOrderReq", description = "创建权益订单")
public class ProductBenefitOrderReq {
    @NotNull(message = "权益id不能为空")
    @Schema(description = "权益id")
    private Long id;
    @NotBlank(message = "支付类型不能为空")
    @Schema(description = "支付类型")
    private String paymentType;
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1)
    @Max(value = 99)
    @Schema(description = "购买数量")
    private Integer purchaseCount;
    private BigDecimal discount = BigDecimal.ONE;
}
