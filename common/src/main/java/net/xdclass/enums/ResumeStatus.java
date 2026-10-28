package net.xdclass.enums;
/**
 * 简历状态枚举
 */
public enum ResumeStatus {
    UPLOADED("已上传"),
    IN_PROCESS("分析中"),
    ANALYZED("已分析"),
    AI_ERROR("ai解析失败"),
    BENEFIT_NOT_ENOUGH("权益不足解析失败");


    private final String description;

    ResumeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}