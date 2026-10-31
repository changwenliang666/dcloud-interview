package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 延迟检查读到的简历状态。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ResumeCheckStatusDTO", description = "简历分析状态")
public class ResumeCheckStatusDTO {

    @Schema(description = "简历状态")
    private String status;

    @Schema(description = "Serverless 是否已接收本次分析")
    private Boolean dispatched;
}
