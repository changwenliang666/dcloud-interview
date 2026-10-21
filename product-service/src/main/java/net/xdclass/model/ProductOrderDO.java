package net.xdclass.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 购买订单表
 * </p>
 *
 * @author WenliangChang
 * @since 2026-10-15
 */
@Getter
@Setter
@Builder
@TableName("product_order")
@Schema(name = "ProductOrderDO", description = "购买订单表")
public class ProductOrderDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户账号ID")
    @TableField("account_id")
    private Long accountId;

    @Schema(description = "订单类型(package/benefit)")
    @TableField("order_type")
    private String orderType;

    @Schema(description = "商品标题")
    @TableField("title")
    private String title;

    @Schema(description = "套餐ID")
    @TableField("product_package_id")
    private Long productPackageId;

    @Schema(description = "权益ID")
    @TableField("benefit_id")
    private Long benefitId;

    @Schema(description = "购买次数")
    @TableField("purchase_count")
    private Integer purchaseCount;

    @Schema(description = "单次价格")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    @Schema(description = "折扣")
    @TableField("discount")
    private BigDecimal discount;

    @Schema(description = "支付金额")
    @TableField("pay_amount")
    private BigDecimal payAmount;

    @Schema(description = "订单状态")
    @TableField("order_state")
    private String orderState;

    @Schema(description = "支付方式")
    @TableField("pay_type")
    private String payType;

    @Schema(description = "第三方流水号")
    @TableField("transaction_no")
    private String transactionNo;

    @Schema(description = "订单编号")
    @TableField("out_trade_no")
    private String outTradeNo;

    @Schema(description = "支付回调时间")
    @TableField("notify_time")
    private Date notifyTime;

    @Schema(description = "创建时间")
    @TableField("gmt_create")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    @TableField("gmt_modified")
    private Date gmtModified;
}
