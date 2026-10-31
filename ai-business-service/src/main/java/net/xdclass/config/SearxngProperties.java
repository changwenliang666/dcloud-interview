package net.xdclass.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "searxng")
@Data
public class SearxngProperties {
    /** SearXNG 服务地址 */
    private String baseUrl;

    /** 返回格式（默认 json） */
    private String format = "json";

    /** 搜索语言（默认中文） */
    private String language = "zh";

    /** 搜索引擎（多个用逗号分隔） */
    private String engines = "quark";
}
