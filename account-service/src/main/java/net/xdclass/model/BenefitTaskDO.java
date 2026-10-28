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
 * 权益任务表(本地事务消息表)
 * </p>
 *
 * @author WenliangChang
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("benefit_task")
@Schema(name = "BenefitTaskDO", description = "权益任务表(本地事务消息表)")
public class BenefitTaskDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("account_id")
    private Long accountId;

    @Schema(description = "用户权益记录ID")
    @TableField("account_benefit_id")
    private Long accountBenefitId;

    @Schema(description = "权益编码")
    @TableField("benefit_code")
    private String benefitCode;

    @Schema(description = "使用次数(扣减次数)")
    @TableField("use_times")
    private Integer useTimes;

    @Schema(description = "关联业务ID(如面试ID)")
    @TableField("business_id")
    private Long businessId;

    @Schema(description = "消息唯一标识(UUID)")
    @TableField("message_id")
    private String messageId;

    @Schema(description = "锁定状态:LOCK-锁定, FINISH-完成, CANCEL-取消")
    @TableField("lock_state")
    private String lockState;

    @Schema(description = "创建时间")
    @TableField("gmt_create")
    private Date gmtCreate;

    @Schema(description = "修改时间")
    @TableField("gmt_modified")
    private Date gmtModified;
}
