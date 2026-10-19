package net.xdclass.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信登录配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.login")
public class WechatLoginConfig {

    /**
     * 微信公众号AppId
     */
    private String appId;

    /**
     * 微信公众号AppSecret
     */
    private String appSecret;


    /**
     * 二维码过期时间（秒）
     */
    private Integer qrCodeExpireTime = 120;

    /**
     * 登录状态过期时间（秒）
     */
    private Integer loginStateExpireTime = 300;

    /**
     * 微信服务器验证Token
     * -- GETTER --
     *  获取配置的token

     */
    private String token;

    /**
     * 微信API基础URL
     */
    private String baseUrl = "https://api.weixin.qq.com";

    /**
     * 通过ticket换取二维码的URL
     */
    private String showQrCodeUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode";

    // ========== 动态构建的API地址 ==========

    /**
     * 获取access_token的URL
     */
    public String getAccessTokenUrl() {
        return baseUrl + "/cgi-bin/token";
    }

    /**
     * 创建二维码ticket的URL
     */
    public String getQrCodeUrl() {
        return baseUrl + "/cgi-bin/qrcode/create";
    }

    /**
     * 获取用户信息的URL
     */
    public String getUserInfoUrl() {
        return baseUrl + "/cgi-bin/user/info";
    }

}