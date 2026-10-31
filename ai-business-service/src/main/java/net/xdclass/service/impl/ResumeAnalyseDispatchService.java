package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.AiEngineProperties;
import net.xdclass.dto.AiEngineAnalyseReq;
import net.xdclass.dto.ResumeAnalyseMessageDTO;
import net.xdclass.enums.ResumeStatus;
import net.xdclass.feign.AccountBenefitFeign;
import net.xdclass.mapper.ResumeMapper;
import net.xdclass.model.ResumeDO;
import net.xdclass.service.ResumeService;
import net.xdclass.util.JsonData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Objects;

/**
 * 消费简历分析消息，向 Serverless 发异步调用。
 * 只等待平台返回已接收，分析结果由引擎回调写回。
 */
@Slf4j
@Service
public class ResumeAnalyseDispatchService {

    public static final String ACCEPTED = "ACCEPTED";
    public static final String DROP = "DROP";

    private static final String FC_ASYNC_HEADER = "X-Fc-Invocation-Type";
    private static final int ACCEPTED_FLAG = 1;

    private final ResumeMapper resumeMapper;
    private final ResumeService resumeService;
    private final AccountBenefitFeign accountBenefitFeign;
    private final AiEngineProperties aiEngineProperties;
    private final RestTemplate aiEngineRestTemplate;

    public ResumeAnalyseDispatchService(ResumeMapper resumeMapper,
                                        ResumeService resumeService,
                                        AccountBenefitFeign accountBenefitFeign,
                                        AiEngineProperties aiEngineProperties) {
        this.resumeMapper = resumeMapper;
        this.resumeService = resumeService;
        this.accountBenefitFeign = accountBenefitFeign;
        this.aiEngineProperties = aiEngineProperties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(aiEngineProperties.getConnectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(aiEngineProperties.getReadTimeoutMs()));
        this.aiEngineRestTemplate = new RestTemplate(factory);
    }

    public String dispatch(ResumeAnalyseMessageDTO message, boolean redelivered) {
        if (message == null || message.getResumeId() == null || message.getAccountId() == null
                || StringUtils.isBlank(message.getMessageId())) {
            log.warn("简历分析消息缺少必要字段");
            return ACCEPTED;
        }
        ResumeDO resumeDO = resumeMapper.selectById(message.getResumeId());
        if (resumeDO == null || !message.getAccountId().equals(resumeDO.getAccountId())) {
            log.warn("简历不存在或账号不匹配, resumeId={}, messageId={}", message.getResumeId(), message.getMessageId());
            return ACCEPTED;
        }
        if (!ResumeStatus.IN_PROCESS.name().equals(resumeDO.getStatus())) {
            log.info("简历不在分析中，跳过投递, resumeId={}, status={}, messageId={}",
                    resumeDO.getId(), resumeDO.getStatus(), message.getMessageId());
            return ACCEPTED;
        }
        if (StringUtils.isBlank(resumeDO.getContent())) {
            log.error("简历内容为空，结束分析, resumeId={}, messageId={}", resumeDO.getId(), message.getMessageId());
            resumeService.failAnalyse(resumeDO.getId(), resumeDO.getAccountId(), message.getMessageId(), ResumeStatus.AI_ERROR);
            return ACCEPTED;
        }
        if (message.getMessageId().equals(resumeDO.getAnalyseMessageId()) && Integer.valueOf(ACCEPTED_FLAG).equals(resumeDO.getAnalyseAccepted())) {
            log.info("分析请求已被Serverless接收, resumeId={}, messageId={}", resumeDO.getId(), message.getMessageId());
            return ACCEPTED;
        }

        boolean claimedNow = false;
        if (StringUtils.isBlank(resumeDO.getAnalyseMessageId())) {
            claimedNow = claim(resumeDO.getId(), resumeDO.getAccountId(), message.getMessageId());
            if (!claimedNow) {
                log.info("分析投递已被其他消费者占用, resumeId={}, messageId={}", resumeDO.getId(), message.getMessageId());
                return ACCEPTED;
            }
        } else if (!(message.getMessageId().equals(resumeDO.getAnalyseMessageId()) && redelivered)) {
            log.info("重复的分析消息，跳过, resumeId={}, messageId={}, redelivered={}",
                    resumeDO.getId(), message.getMessageId(), redelivered);
            return ACCEPTED;
        }

        try {
            boolean accepted = invokeAsync(resumeDO, message.getMessageId());
            if (!accepted) {
                releaseClaim(resumeDO.getId(), resumeDO.getAccountId(), message.getMessageId());
                return DROP;
            }
            markAccepted(resumeDO.getId(), resumeDO.getAccountId(), message.getMessageId());
            scheduleDeadline(message.getMessageId(), resumeDO.getId());
            log.info("Serverless已接收简历分析, resumeId={}, messageId={}", resumeDO.getId(), message.getMessageId());
            return ACCEPTED;
        } catch (Exception e) {
            log.error("调用Serverless失败, resumeId={}, messageId={}", resumeDO.getId(), message.getMessageId(), e);
            releaseClaim(resumeDO.getId(), resumeDO.getAccountId(), message.getMessageId());
            return DROP;
        }
    }

    private boolean claim(Long resumeId, Long accountId, String messageId) {
        int rows = resumeMapper.update(new LambdaUpdateWrapper<ResumeDO>()
                .set(ResumeDO::getAnalyseMessageId, messageId)
                .set(ResumeDO::getAnalyseAccepted, 0)
                .eq(ResumeDO::getId, resumeId)
                .eq(ResumeDO::getAccountId, accountId)
                .eq(ResumeDO::getStatus, ResumeStatus.IN_PROCESS.name())
                .isNull(ResumeDO::getAnalyseMessageId));
        return rows == 1;
    }

    private void releaseClaim(Long resumeId, Long accountId, String messageId) {
        int rows = resumeMapper.update(new LambdaUpdateWrapper<ResumeDO>()
                .setSql("analyse_message_id = null")
                .set(ResumeDO::getAnalyseAccepted, 0)
                .eq(ResumeDO::getId, resumeId)
                .eq(ResumeDO::getAccountId, accountId)
                .eq(ResumeDO::getStatus, ResumeStatus.IN_PROCESS.name())
                .eq(ResumeDO::getAnalyseMessageId, messageId)
                .eq(ResumeDO::getAnalyseAccepted, 0));
        log.info("释放分析投递占用, resumeId={}, messageId={}, rows={}", resumeId, messageId, rows);
    }

    private void markAccepted(Long resumeId, Long accountId, String messageId) {
        int rows = resumeMapper.update(new LambdaUpdateWrapper<ResumeDO>()
                .set(ResumeDO::getAnalyseAccepted, ACCEPTED_FLAG)
                .eq(ResumeDO::getId, resumeId)
                .eq(ResumeDO::getAccountId, accountId)
                .eq(ResumeDO::getStatus, ResumeStatus.IN_PROCESS.name())
                .eq(ResumeDO::getAnalyseMessageId, messageId));
        if (rows != 1) {
            log.warn("标记Serverless已接收未生效, resumeId={}, messageId={}", resumeId, messageId);
        }
    }

    private boolean invokeAsync(ResumeDO resumeDO, String messageId) {
        if (StringUtils.isBlank(aiEngineProperties.getBaseUrl()) || StringUtils.isBlank(aiEngineProperties.getCallbackUrl())) {
            log.error("未配置Serverless地址或回调地址, resumeId={}", resumeDO.getId());
            return false;
        }
        AiEngineAnalyseReq req = AiEngineAnalyseReq.builder()
                .resumeContent(resumeDO.getContent())
                .callback(aiEngineProperties.getCallbackUrl())
                .accountId(resumeDO.getAccountId())
                .resumeId(resumeDO.getId())
                .messageId(messageId)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(FC_ASYNC_HEADER, "Async");
        String baseUrl = aiEngineProperties.getBaseUrl().endsWith("/")
                ? aiEngineProperties.getBaseUrl().substring(0, aiEngineProperties.getBaseUrl().length() - 1)
                : aiEngineProperties.getBaseUrl();
        String analysePath = aiEngineProperties.getAnalysePath().startsWith("/")
                ? aiEngineProperties.getAnalysePath()
                : "/" + aiEngineProperties.getAnalysePath();
        String url = baseUrl + analysePath;
        ResponseEntity<String> response = aiEngineRestTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(req, headers), String.class);
        int status = response.getStatusCode().value();
        log.info("Serverless异步调用返回, resumeId={}, messageId={}, status={}", resumeDO.getId(), messageId, status);
        return status == 202;
    }

    private void scheduleDeadline(String messageId, Long resumeId) {
        try {
            JsonData result = accountBenefitFeign.scheduleAnalyseDeadline(messageId, resumeId);
            if (result == null || !Objects.equals(result.getCode(), 0)) {
                log.error("登记接收后超时检查失败, messageId={}, code={}", messageId, result == null ? null : result.getCode());
                return;
            }
            log.info("登记接收后超时检查成功, messageId={}", messageId);
        } catch (Exception e) {
            log.error("登记接收后超时检查异常, messageId={}", messageId, e);
        }
    }
}
