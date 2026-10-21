package net.xdclass.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductPackageOrderReq {
    @NotNull(message = "套餐id不能为空")
    @Schema(description = "套餐id")
    private Long id;
    @NotBlank(message = "支付类型不能为空")
    @Schema(description = "支付类型")
    private String paymentType;
    @Schema(description = "购买数量")
    private Integer purchaseCount  = 1;
    private BigDecimal discount = BigDecimal.ONE;
}
