package net.xdclass.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "ResumeAnalyseCallbackReq", description = "Serverless简历分析回调")
public class ResumeAnalyseCallbackReq {

    @NotNull(message = "简历ID不能为空")
    @Schema(description = "简历ID")
    private Long resumeId;

    @NotNull(message = "账号ID不能为空")
    @Schema(description = "账号ID")
    private Long accountId;

    @NotBlank(message = "消息ID不能为空")
    @Schema(description = "权益任务消息ID")
    private String messageId;

    @NotNull(message = "分析是否成功不能为空")
    @Schema(description = "分析是否成功")
    private Boolean success;

    @Schema(description = "整体评价")
    private String evaluation;

    @Schema(description = "技能标签")
    private List<String> skillTags;
}
