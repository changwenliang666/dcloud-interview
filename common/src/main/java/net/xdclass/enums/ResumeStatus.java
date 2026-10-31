package net.xdclass.enums;

import java.util.List;

/**
 * 简历状态枚举
 */
public enum ResumeStatus {
    UPLOADED("已上传"),
    IN_PROCESS("分析中"),
    ANALYZED("已分析"),
    AI_ERROR("ai解析失败"),
    BENEFIT_NOT_ENOUGH("权益不足解析失败"),
    ANALYZE_TIMEOUT("分析超时"),
    UNKOWN_ERROR("未知错误");


    private final String description;

    ResumeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    public static boolean isProcessing(String status) {
        return IN_PROCESS.name().equals(status);
    }

    /**
     * 已明确失败，可以退回权益。
     */
    public static boolean isResumeAnalyzeError(String status) {
        return !List.of(UPLOADED.name(), ANALYZED.name(), IN_PROCESS.name()).contains(status);
    }
}