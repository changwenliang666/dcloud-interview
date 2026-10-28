package net.xdclass.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.InterviewEventMQConfig;
import net.xdclass.dto.ResumeAnalyseMessageDTO;
import net.xdclass.enums.BenefitEnum;
import net.xdclass.strategy.BenefitMessageStrategy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 简历分析消息发送策略
 *
 * 设计说明：
 * - 策略实现在ai-service中
 * - 负责发送简历分析业务MQ消息
 * - 消息由ai-service的消费者处理
 *
 * @author 小滴课堂
 */
@Component
@Slf4j
public class ResumeAnalyseMessageStrategy implements BenefitMessageStrategy {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendBusinessMessage(String messageId, Long businessId, Long accountId) {
        // 构建消息DTO
        ResumeAnalyseMessageDTO messageDTO = ResumeAnalyseMessageDTO.builder()
                .resumeId(businessId)
                .accountId(accountId)
                .messageId(messageId)
                .build();

        // 发送到简历分析队列
        rabbitTemplate.convertAndSend(
                InterviewEventMQConfig.INTERVIEW_EVENT_EXCHANGE,
                InterviewEventMQConfig.RESUME_ANALYSE_ROUTING_KEY,
                messageDTO
        );

        log.info("发送简历分析业务消息成功, messageId={}, resumeId={}", messageId, businessId);
    }

    @Override
    public String getStrategyType() {
        return BenefitEnum.RESUME_ANALYSE.name();
    }
}