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
 * 套餐表
 * </p>
 *
 * @author WenliangChang
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("product_package")
@Schema(name = "ProductPackageDO", description = "套餐表")
public class ProductPackageDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "套餐ID")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "套餐名称")
    @TableField("name")
    private String name;

    @Schema(description = "套餐描述")
    @TableField("description")
    private String description;

    @Schema(description = "套餐价格")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "有效期(天)")
    @TableField("valid_days")
    private Integer validDays;

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
