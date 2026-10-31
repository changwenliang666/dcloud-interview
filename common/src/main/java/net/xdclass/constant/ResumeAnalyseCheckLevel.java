package net.xdclass.constant;

/**
 * 简历分析延迟检查档位。
 * 1、2 档只观察或补投递；3、4 档才会把仍在分析中的简历判为超时。
 */
public final class ResumeAnalyseCheckLevel {

    /** 扣次后 1 分钟：尚未占用投递时补发分析消息 */
    public static final int PROBE = 1;

    /** 扣次后 5 分钟：仍未占用投递时再补发一次 */
    public static final int REDISPATCH = 2;

    /** Serverless 接收成功后 8 分钟：回调仍未落库则超时 */
    public static final int INVOKE_DEADLINE = 3;

    /** 扣次后 20 分钟：无论是否已接收，仍在分析中则超时 */
    public static final int ABSOLUTE_DEADLINE = 4;

    private ResumeAnalyseCheckLevel() {
    }

    public static boolean isForceTimeout(Integer checkLevel) {
        return checkLevel != null && checkLevel >= INVOKE_DEADLINE;
    }
}
