package net.xdclass.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "ProductOrderPageReq", description = "分页查询用户订单列表参数")
public class ProductOrderPageReq {
    @NotNull
    @Min(value = 1)
    private Integer pageIndex;

    @NotNull
    @Min(value = 10)
    @Max(value = 100)
    private Integer pageNum;

    private String orderType;
    private String payType;
    private String orderState;
}
