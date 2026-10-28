package net.xdclass.enums;

public enum BenefitTaskStateEnum {
    /**
     * 锁定状态 - 初始状态,权益已扣减,等待面试创建完成
     */
    LOCK("LOCK", "锁定"),

    /**
     * 完成状态 - 面试创建成功
     */
    FINISH("FINISH", "完成"),

    /**
     * 取消状态 - 面试创建失败,权益已补偿恢复
     */
    CANCEL("CANCEL", "取消");

    private final String code;
    private final String desc;

    BenefitTaskStateEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举,未找到返回null
     */
    public static BenefitTaskStateEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (BenefitTaskStateEnum state : values()) {
            if (state.getCode().equals(code)) {
                return state;
            }
        }
        return null;
    }

    /**
     * 判断是否为完成状态
     */
    public boolean isFinish() {
        return this == FINISH;
    }

    /**
     * 判断是否为锁定状态
     */
    public boolean isLock() {
        return this == LOCK;
    }

    /**
     * 判断是否为取消状态
     */
    public boolean isCancel() {
        return this == CANCEL;
    }
}
