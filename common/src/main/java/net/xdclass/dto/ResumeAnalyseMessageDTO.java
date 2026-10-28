package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 简历分析MQ消息DTO
 * 用于ai-service发送简历分析任务消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ResumeAnalyseMessageDTO", description = "简历分析MQ消息")
public class ResumeAnalyseMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "账号ID")
    private Long accountId;

    @Schema(description = "消息ID（用于关联延迟检查）")
    private String messageId;
}
