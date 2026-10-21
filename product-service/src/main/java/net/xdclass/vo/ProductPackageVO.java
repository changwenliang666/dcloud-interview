package net.xdclass.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 套餐列表展示对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProductPackageVO", description = "套餐信息")
public class ProductPackageVO {

    @Schema(description = "套餐ID")
    private Long id;

    @Schema(description = "套餐名称")
    private String name;

    @Schema(description = "套餐描述")
    private String description;

    @Schema(description = "套餐价格")
    private BigDecimal price;

    @Schema(description = "有效期(天)")
    private Integer validDays;

    @Schema(description = "是否推荐")
    private Boolean isRecommended;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    private Date gmtModified;

    @Schema(description = "套餐包含的权益")
    private List<PackageBenefitVO> benefitList;
}
