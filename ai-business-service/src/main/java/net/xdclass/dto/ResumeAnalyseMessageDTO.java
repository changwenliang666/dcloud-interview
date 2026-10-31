package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ResumeAnalyseMessageDTO",description = "简历分析队列消息DTO")
public class ResumeAnalyseMessageDTO {
    @Schema(description = "简历id")
    private Long resumeId;
    @Schema(description = "账号id")
    private Long accountId;
    @Schema(description = "消息id")
    private String messageId;
}
