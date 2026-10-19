package net.xdclass.config;

public class RedisKeyManager {
    public static final String CHECK_CODE_KEY = "check_code:%s:%s";
    /**
     * 微信access_token缓存,加入appID隔离
     * "wechat:access_token"
     */
    public static final String WECHAT_ACCESS_TOKEN = "znoffer:wechat:access_token:%s";

    /**
     * access_token 分布式锁，按 appId 隔离
     * 重新构建 access_token 缓存时使用
     */
    public static final String WECHAT_ACCESS_TOKEN_LOCK = "znoffer:wechat:access_token:lock:%s";

    /**
     * 微信登录二维码状态
     */
    public static final String WECHAT_QR_CODE_STATUS = "znoffer:wechat:qr_code:status:%s";

    /**
     * 存储用户登录结果
     * %s就是场景id
     */
    public static final String WECHAT_LOGIN_RESULT = "znoffer:wechat:login:result:%s";
}
