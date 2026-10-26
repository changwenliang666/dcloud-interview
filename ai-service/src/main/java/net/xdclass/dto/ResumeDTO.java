package net.xdclass.dto;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author WenliangChang
 * @since 2026-10-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ResumeDO", description = "简历表")
public class ResumeDTO {
    @Schema(description = "简历ID")
    private Long id;

    @Schema(description = "账号ID")
    private Long accountId;

    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "简历内容")
    private String content;

    @Schema(description = "技能标签")
    private String skillTags;

    @Schema(description = "AI简历分析")
    private String evaluation;

    @Schema(description = "解析状态")
    private String status;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    private Date gmtModified;
}
