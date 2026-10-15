package net.xdclass.enums;

import lombok.Getter;

/**
 * 错误码枚举类
 */
@Getter
public enum BizCodeEnum {

    /**
     * 通用操作码
     */
    OPS_REPEAT(110001, "重复操作"),
    OPS_NETWORK_ADDRESS_ERROR(110002, "网络地址错误"),

    /**
     * 账号相关错误码
     */
    ACCOUNT_REPEAT(110003, "账号已经存在"),
    ACCOUNT_UNREGISTER(110004, "账号不存在"),
    ACCOUNT_PWD_ERROR(110005, "账号或者密码错误"),
    ACCOUNT_UNLOGIN(110006, "账号未登录"),
    ACCOUNT_NO_AUTH(110007, "无权限访问"),

    /**
     *验证码
     */
    CODE_TO_ERROR(240001,"接收号码不合规"),
    CODE_LIMITED(240002,"验证码发送过快"),
    CODE_ERROR(240003,"验证码错误"),
    CODE_CAPTCHA_ERROR(240101,"图形验证码错误"),

    /**
     * 权益
     */
    BENEFIT_NOT_EXIST(110016, "权益不存在或已下架"),
    BENEFIT_NOT_ENOUGH(110017, "权益不足"),



    /**
     * 验证码相关错误码
     */
    CAPTCHA_EXPIRED(110008, "验证码已过期"),
    CAPTCHA_ERROR(110009, "验证码错误"),
    SMS_CODE_ERROR(110018, "短信验证码错误"),

    /**
     * 文件相关错误码
     */
    FILE_UPLOAD_USER_IMG_FAIL(110010, "用户头像文件上传失败"),

    /**
     * 数据库默认值
     */
    SYSTEM_ERROR(999999, "系统错误"),

    //余额不够
    BALANCE_NOT_ENOUGH(110011, "余额不足"),

    //订单
    ORDER_NO_EXIST(110012, "订单不存在"),

    NO_PERMISSION(403001,"权限不够"),

    PRODUCT_NOT_EXIST(220001, "商品不存在"),

    BANNER_NOT_EXIST(220002, "Banner不存在"),

    REPEAT_SUBMIT_IDEMPOTENT(20001,"重复提交"),

    MQ_CONSUME_EXCEPTION(41231,"消息消费失败" );

    private final int code;
    private final String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }


}