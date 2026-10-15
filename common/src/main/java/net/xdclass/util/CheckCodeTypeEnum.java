package net.xdclass.util;

import lombok.Getter;

/**
 * 验证码状态类型
 */
@Getter
public enum CheckCodeTypeEnum {
    /**
     * 登录
     */
    LOGIN,
    /**
     * 绑定
     */
    BIND
}