package net.xdclass.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author WenliangChang
 * @since 2026-10-15
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume")
@Schema(name = "ResumeDO", description = "简历表")
public class ResumeDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "简历ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "账号ID")
    @TableField("account_id")
    private Long accountId;

    @Schema(description = "文件名")
    @TableField("filename")
    private String filename;

    @Schema(description = "文件路径")
    @TableField("file_path")
    private String filePath;

    @Schema(description = "文件类型")
    @TableField("file_type")
    private String fileType;

    @Schema(description = "简历内容")
    @TableField("content")
    private String content;

    @Schema(description = "技能标签")
    @TableField("skill_tags")
    private String skillTags;

    @Schema(description = "AI简历分析")
    @TableField("evaluation")
    private String evaluation;

    @Schema(description = "解析状态")
    @TableField("status")
    private String status;

    @Schema(description = "分析任务消息ID")
    @TableField("analyse_message_id")
    private String analyseMessageId;

    @Schema(description = "Serverless是否已接收：0否 1是")
    @TableField("analyse_accepted")
    private Integer analyseAccepted;

    @Schema(description = "创建时间")
    @TableField("gmt_create")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    @TableField("gmt_modified")
    private Date gmtModified;
}
