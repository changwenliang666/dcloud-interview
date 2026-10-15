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
 * 权益使用日志表
 * </p>
 *
 * @author WenliangChang
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("benefit_usage_log")
@Schema(name = "BenefitUsageLogDO", description = "权益使用日志表")
public class BenefitUsageLogDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户账号ID（关联account表id）")
    @TableField("account_id")
    private Long accountId;

    @Schema(description = "使用权益ID（关联interview，resume表id）")
    @TableField("biz_id")
    private Long bizId;

    @Schema(description = "权益编码")
    @TableField("benefit_code")
    private String benefitCode;

    @Schema(description = "用户权益ID（关联account_benefit表id）")
    @TableField("account_benefit")
    private Long accountBenefit;

    @Schema(description = "使用时间")
    @TableField("usage_time")
    private Date usageTime;

    @Schema(description = "使用备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "创建时间")
    @TableField("gmt_create")
    private Date gmtCreate;
}
