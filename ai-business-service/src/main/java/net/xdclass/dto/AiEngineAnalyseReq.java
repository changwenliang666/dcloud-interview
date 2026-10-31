package net.xdclass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发给 ai-engine-service 的分析请求，字段与引擎侧 AnalyseResumeReq 一致。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEngineAnalyseReq {

    private String resumeContent;

    private String callback;

    private Long accountId;

    private Long resumeId;

    private String messageId;
}
