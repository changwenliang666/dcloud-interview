package net.xdclass.config;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.interceptor.AccountLoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new AccountLoginInterceptor())
                //拦截的路径
                .addPathPatterns("/api/*/**")

                //不拦截的路径
                .excludePathPatterns(
                        "/api/v1/account/login",
                        "/api/v1/account/get-captcha",
                        "/api/v1/account/send-check-code",
                        "/api/v1/banner/*",
                        // 微信登录相关接口
                        "/api/v1/wechat/qrcode",
                        "/api/v1/wechat/qrcode/status",
                        "/api/v1/wechat/login/result",
                        "/api/v1/wechat/auth/url",
                        "/api/v1/wechat/auth/callback",
                        // 微信回调接口
                        "/api/v1/wechat/callback/**",
                        "/api/v1/wechat/scan/callback",
                        // 产品相关接口（不需要登录）
                        "/api/v1/product/packages",
                        "/api/v1/product/benefits",
                        "/api/v1/product/package/detail",
                        "/api/v1/product/benefit/detail",
                        // 支付回调接口（不需要登录）
                        "/api/v1/order/callback/wechat_pay",
                        "/api/v1/order/callback/alipay",
                        // 静态资源
                        "/favicon.ico",
                        "/error")
                .order(1); // 优先级
    }
}
