package net.xdclass.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.engine")
public class AiEngineCallbackProperties {

    private String callbackToken = "";

    private int callbackRetry = 3;
}
