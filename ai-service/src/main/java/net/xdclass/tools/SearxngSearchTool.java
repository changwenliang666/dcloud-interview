package net.xdclass.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.SearxngProperties;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearxngSearchTool implements BiFunction<SearxngSearchTool.SearchRequest, ToolContext, String> {

    private final RestTemplate restTemplate;
    private final SearxngProperties searxngProperties;

    @Override
    public String apply(SearchRequest request, ToolContext toolContext) {
        return search(request.getKeyword());
    }

    /**
     * 核心搜索方法：请求 SearXNG REST API 并格式化结果
     */
    public String search(String keyword) {
        log.info("SearXNG 搜索开始, keyword:{}", keyword);
        try {
            String url = searxngProperties.getBaseUrl() + "/search";

            // 构造请求参数（对应 SearXNG API 的查询参数）
            Map<String, Object> params = new HashMap<>();
            params.put("q", keyword);
            params.put("format", searxngProperties.getFormat());
            params.put("language", searxngProperties.getLanguage());
            params.put("engines", searxngProperties.getEngines());

            // 发起 GET 请求，自动将 JSON 反序列化为 SearxngResponse
            SearxngResponse response = restTemplate.getForObject(
                    url + "?q={q}&format={format}&language={language}&engines={engines}",
                    SearxngResponse.class,
                    params
            );

            // 判空处理
            if (response == null || CollectionUtils.isEmpty(response.getResults())) {
                log.warn("SearXNG 搜索无结果, keyword:{}", keyword);
                return "未找到相关搜索结果";
            }

            String result = formatResults(response.getResults());
            log.info("SearXNG 搜索完成, resultCount:{}", response.getResults().size());
            return result;

        } catch (Exception e) {
            log.error("SearXNG 搜索异常, keyword:{}", keyword, e);
            return "搜索服务暂时不可用，请稍后重试";
        }
    }

    /**
     * 将搜索结果列表格式化为文本，供大模型阅读理解
     */
    private String formatResults(List<SearchResult> results) {
        return results.stream()
                .map(r -> String.format("标题: %s\n链接: %s\n摘要: %s",
                        r.getTitle(), r.getUrl(), r.getContent()))
                .collect(Collectors.joining("\n\n"));
    }

    // ========== 内部响应模型 ==========

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearxngResponse {
        private List<SearchResult> results;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchResult {
        private String title;    // 搜索结果标题
        private String url;      // 原文链接
        private String content;  // 摘要内容
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private String keyword;
    }

}
