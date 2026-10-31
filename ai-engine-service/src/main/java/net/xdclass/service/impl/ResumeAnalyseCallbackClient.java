package net.xdclass.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.AiEngineCallbackProperties;
import net.xdclass.dto.ResumeAnalyseCallbackPayload;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 在同一次函数调用内把分析结果回调给业务系统，失败会重试。
 */
@Slf4j
@Component
public class ResumeAnalyseCallbackClient {

    private static final String CALLBACK_TOKEN_HEADER = "X-Callback-Token";

    private final RestTemplate callbackRestTemplate;
    private final AiEngineCallbackProperties properties;

    public ResumeAnalyseCallbackClient(@Qualifier("callbackRestTemplate") RestTemplate callbackRestTemplate,
                                       AiEngineCallbackProperties properties) {
        this.callbackRestTemplate = callbackRestTemplate;
        this.properties = properties;
    }

    public boolean notify(String callbackUrl, ResumeAnalyseCallbackPayload payload) {
        int retry = Math.max(properties.getCallbackRetry(), 1);
        for (int attempt = 1; attempt <= retry; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set(CALLBACK_TOKEN_HEADER, properties.getCallbackToken());
                String body = callbackRestTemplate.postForObject(callbackUrl, new HttpEntity<>(payload, headers), String.class);
                JSONObject result = JSON.parseObject(body);
                if (result != null && Integer.valueOf(0).equals(result.getInteger("code"))) {
                    log.info("简历分析回调成功, resumeId={}, messageId={}, attempt={}",
                            payload.getResumeId(), payload.getMessageId(), attempt);
                    return true;
                }
                log.warn("简历分析回调被拒绝, resumeId={}, messageId={}, attempt={}, body={}",
                        payload.getResumeId(), payload.getMessageId(), attempt, body);
            } catch (Exception e) {
                log.error("简历分析回调失败, resumeId={}, messageId={}, attempt={}",
                        payload.getResumeId(), payload.getMessageId(), attempt, e);
            }
            if (attempt < retry) {
                sleepBeforeRetry(attempt);
            }
        }
        return false;
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(500L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
