package net.xdclass.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 权益表
 * </p>
 *
 * @author WenliangChang
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("benefit")
@Schema(name = "BenefitDO", description = "权益表")
public class BenefitDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "权益ID")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "权益名称")
    @TableField("name")
    private String name;

    @Schema(description = "权益编码")
    @TableField("benefit_code")
    private String benefitCode;

    @Schema(description = "权益描述")
    @TableField("description")
    private String description;

    @Schema(description = "单次价格")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    @Schema(description = "单次有效期(天)")
    @TableField("valid_days")
    private Integer validDays;

    @Schema(description = "是否热门")
    @TableField("is_hot")
    private Boolean isHot;

    @Schema(description = "是否推荐")
    @TableField("is_recommended")
    private Boolean isRecommended;

    @Schema(description = "状态")
    @TableField("status")
    private String status;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "创建时间")
    @TableField("gmt_create")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    @TableField("gmt_modified")
    private Date gmtModified;
}
