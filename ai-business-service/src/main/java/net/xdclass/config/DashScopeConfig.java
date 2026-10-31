package net.xdclass.config;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeChatProperties;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.tools.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DashScope AI配置类
 * 配置ChatModel供各Agent使用
 */

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DashScopeConfig {

    private final WeatherTool weatherTool;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    /**
     * 配置DashScope ChatModel
     *
     */
    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder().apiKey(apiKey).build();
    }

    @Bean
    public DashScopeChatModel dashScopeChatModel(DashScopeApi dashScopeApi, DashScopeChatProperties chatProperties) {
        DashScopeChatOptions options = DashScopeChatOptions.fromOptions(chatProperties.getOptions());
        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(options)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel dashScopeChatModel) {
        return ChatClient
                .builder(dashScopeChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}