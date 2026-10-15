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
 * 
 * </p>
 *
 * @author 小滴课堂-二当家小D,
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("resume")
@Schema(name = "ResumeDO", description = "")
public class ResumeDO implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("account_id")
    private Long accountId;

    @TableField("filename")
    private String filename;

    @TableField("file_path")
    private String filePath;

    @TableField("file_type")
    private String fileType;

    @TableField("content")
    private String content;

    @TableField("skill_tags")
    private String skillTags;

    @Schema(description = "AI简历分析")
    @TableField("evaluation")
    private String evaluation;

    @Schema(description = "解析状态")
    @TableField("status")
    private String status;

    @TableField("gmt_create")
    private Date gmtCreate;

    @TableField("gmt_modified")
    private Date gmtModified;
}
