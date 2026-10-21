package net.xdclass.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 权益列表展示对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BenefitVO", description = "权益信息")
public class BenefitVO {

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

    @Schema(description = "状态")
    private String status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    private Date gmtModified;
}
