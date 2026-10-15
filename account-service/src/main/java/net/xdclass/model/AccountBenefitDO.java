package net.xdclass.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户权益表
 * </p>
 *
 * @author WenliangChang
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("account_benefit")
@Schema(name = "AccountBenefitDO", description = "用户权益表")
public class AccountBenefitDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户账号ID")
    @TableField("account_id")
    private Long accountId;

    @Schema(description = "权益编码")
    @TableField("benefit_code")
    private String benefitCode;

    @Schema(description = "来源订单ID")
    @TableField("product_order_id")
    private Long productOrderId;

    @Schema(description = "剩余次数")
    @TableField("remaining_count")
    private Integer remainingCount;

    @Schema(description = "总次数")
    @TableField("total_count")
    private Integer totalCount;

    @Schema(description = "生效时间")
    @TableField("start_time")
    private Date startTime;

    @Schema(description = "到期时间")
    @TableField("end_time")
    private Date endTime;

    @Schema(description = "创建时间")
    @TableField("gmt_create")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    @TableField("gmt_modified")
    private Date gmtModified;
}
