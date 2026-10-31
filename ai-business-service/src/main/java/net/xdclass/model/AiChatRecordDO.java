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
 * AI对话记录（会话+消息合并表）
 * </p>
 *
 * @author WenliangChang
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("ai_chat_record")
@Schema(name = "AiChatRecordDO", description = "AI对话记录（会话+消息合并表）")
public class AiChatRecordDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID（租户ID）")
    @TableField("account_id")
    private Long accountId;

    @Schema(description = "面试ID")
    @TableField("interview_id")
    private Long interviewId;

    @Schema(description = "METADATA-会话元信息，MESSAGE-消息内容")
    @TableField("record_type")
    private String recordType;

    @Schema(description = "角色（user/assistant/system）")
    @TableField("role")
    private String role;

    @Schema(description = "会话标题或消息内容")
    @TableField("content")
    private String content;

    @Schema(description = "消息序号（同会话内递增）")
    @TableField("sequence")
    private Integer sequence;

    @Schema(description = "创建时间")
    @TableField("gmt_create")
    private Date gmtCreate;

    @Schema(description = "会话状态（ONGOING-进行中，ENDED-已结束）")
    @TableField("status")
    private String status;
}
