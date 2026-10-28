package net.xdclass.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.tools.SearxngSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class AiController {

    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final SearxngSearchTool searxngSearchTool;

    @GetMapping("/chat1")
    public String chat1(@RequestParam(value = "message") String message) {
        ToolCallback searchTool = FunctionToolCallback.builder("web_search", searxngSearchTool)
                .description("联网搜索工具，用于搜索实时信息、新闻、知识、数据、当前时间日期等内容")
                .inputType(SearxngSearchTool.SearchRequest.class)
                .build();
        return chatClient.prompt().user(message).toolCallbacks(searchTool).call().content();
    }

    @GetMapping("/chat2")
    public String chat2(@RequestParam(value = "message") String message) {
        SystemMessage systemMessage = new SystemMessage("你是一个智能助手");
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        ChatResponse response = chatModel.call(prompt);
        log.info("调用模型消耗的token:{}", response.getMetadata().getUsage().getTotalTokens());
        return response.getResult().getOutput().getText();
    }

    @GetMapping("/agent1")
    public String agent1(@RequestParam(value = "message") String message) throws GraphRunnerException {
        ReactAgent agent = ReactAgent
                .builder()
                .model(chatModel)
                .name("agent智能体-01")
                .systemPrompt("""
                        你是一个个人助手，用简洁幽默的语言回答用户的问题
                        """)
                .saver(new MemorySaver())
                .build();
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId("agent-01").build();
        agent.call("我是冰冰，我是小滴课堂的助教",runnableConfig);
        AssistantMessage response = agent.call(message,runnableConfig);
        return response.getText();
    }
}
