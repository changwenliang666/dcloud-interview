package net.xdclass.enums;

public enum BenefitEnum {
    //AI简历分析
    RESUME_ANALYSE("AI简历分析"),
    //专项面试
    SPECIAL_INTERVIEW("专项面试"),
    //简历综合面
    COMMON_INTERVIEW("简历综合面");
    private final String title;

    BenefitEnum(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
