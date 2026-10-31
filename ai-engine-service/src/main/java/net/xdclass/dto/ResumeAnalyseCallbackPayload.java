package net.xdclass.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 回传给业务系统的分析结论。字段与业务侧 ResumeAnalyseCallbackReq 一致。
 */
@Data
@Builder
public class ResumeAnalyseCallbackPayload {

    private Long resumeId;

    private Long accountId;

    private String messageId;

    private Boolean success;

    private String evaluation;

    private List<String> skillTags;
}
