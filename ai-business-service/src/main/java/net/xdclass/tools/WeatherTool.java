package net.xdclass.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool {
    /**
     * @Tool 注解标记这是一个可以被 AI 调用的工具
     * description 是给 AI 看的工具说明，越清晰 AI 越会正确使用
     */
    @Tool(description = "查询指定城市的实时天气信息，包括温度、天气状况、湿度等")
    public String getWeather(
            @ToolParam(description = "城市名称，如：北京、上海、杭州") String city) {
        // 实际业务中调用天气API
        // 这里用模拟数据演示
        return String.format("{\"city\":\"%s\", \"temperature\":\"25°C\", " +
                "\"weather\":\"晴\", \"humidity\":\"60%%\"}", city);
    }

    @Tool(description = "根据订单ID查询订单状态和详细信息")
    public String queryOrder(
            @ToolParam(description = "订单ID，格式为纯数字") String orderId) {
        // 实际业务中查询数据库
        return String.format("{\"orderId\":\"%s\", \"status\":\"已发货\", " +
                "\"createTime\":\"2026-03-21\"}", orderId);
    }
}
