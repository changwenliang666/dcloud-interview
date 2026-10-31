package net.xdclass.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name="analyseResumeReq",description = "ai分析简历请求消息")

public class AnalyseResumeReq {
    @NotBlank(message = "简历内容不能为空")
    @Schema(description = "简历内容")
    private String resumeContent;
    @NotBlank(message = "回调地址不能为空")
    @Schema(description = "回调地址")
    private String callback;
    @NotNull(message = "账号id不能为空")
    @Schema(description = "账号id")
    private Long accountId;
    @NotNull(message = "简历id不能为空")
    @Schema(description = "简历id")
    private Long resumeId;

    @NotBlank(message = "消息id不能为空")
    @Schema(description = "权益任务消息id")
    private String messageId;
}
