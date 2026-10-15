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
 * 套餐包含权益表
 * </p>
 *
 * @author 小滴课堂-二当家小D,
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("package_benefit")
@Schema(name = "PackageBenefitDO", description = "套餐包含权益表")
public class PackageBenefitDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "套餐ID")
    @TableField("product_package_id")
    private Long productPackageId;

    @Schema(description = "权益编码")
    @TableField("benefit_code")
    private String benefitCode;

    @Schema(description = "包含次数")
    @TableField("count")
    private Integer count;

    @Schema(description = "创建时间")
    @TableField("gmt_create")
    private Date gmtCreate;
}
