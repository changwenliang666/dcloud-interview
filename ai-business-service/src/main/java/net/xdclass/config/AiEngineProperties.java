package net.xdclass.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.engine")
public class AiEngineProperties {

    /** 函数计算 HTTP 触发地址，不要填引擎进程自己的端口 */
    private String baseUrl;

    private String analysePath = "/api/v1/ai-engine/resume/analyse";

    /** 引擎分析完成后回调本服务的地址 */
    private String callbackUrl;

    private String callbackToken;

    private int connectTimeoutMs = 3000;

    /** 只等待平台返回已接收，不等模型跑完 */
    private int readTimeoutMs = 10000;
}
