package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 套餐内权益展示对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PackageBenefitVO", description = "套餐包含的权益")
public class PackageBenefitVO {

    @Schema(description = "权益ID")
    private Long id;

    @Schema(description = "权益名称")
    private String name;

    @Schema(description = "权益编码")
    private String benefitCode;

    @Schema(description = "权益描述")
    private String description;

    @Schema(description = "单次价格")
    private BigDecimal unitPrice;

    @Schema(description = "单次有效期(天)")
    private Integer validDays;

    @Schema(description = "是否热门")
    private Boolean isHot;

    @Schema(description = "是否推荐")
    private Boolean isRecommended;

    @Schema(description = "包含次数")
    private Integer count;
}
