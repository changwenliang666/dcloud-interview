package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 购买订单DTO
 */
@Data
public class ProductOrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "用户账号ID")
    private Long accountId;

    @Schema(description = "订单类型(PACKAGE_ORDER/BENEFIT_ORDER)")
    private String orderType;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "套餐ID")
    private Long productPackageId;

    @Schema(description = "权益ID")
    private Long benefitId;

    @Schema(description = "购买次数")
    private Integer purchaseCount;

    @Schema(description = "单次价格")
    private BigDecimal unitPrice;

    @Schema(description = "折扣(1.00=无折扣)")
    private BigDecimal discount;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "订单状态：NEW未支付, PAY已支付, CANCEL已取消")
    private String orderState;

    @Schema(description = "支付方式：WECHAT_PAY/ALI_PAY")
    private String payType;

    @Schema(description = "第三方支付流水号")
    private String transactionNo;

    @Schema(description = "商户订单号")
    private String outTradeNo;

    @Schema(description = "支付回调时间")
    private Date notifyTime;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    private Date gmtModified;

    /**
     * 套餐信息（仅套餐订单时使用）
     */
    private ProductPackageVO productPackage;

    /**
     * 权益信息（仅权益订单时使用）
     */
    private BenefitVO benefit;
}