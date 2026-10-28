package net.xdclass.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.InterviewEventMQConfig;
import net.xdclass.dto.InterviewCreateMessageDTO;
import net.xdclass.strategy.BenefitMessageStrategy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 面试创建消息发送策略
 *
 * 设计说明：
 * - 策略实现在ai-service中
 * - 负责发送面试创建业务MQ消息
 * - 支持所有面试类型（专项面试、简历综合面）
 * - 消息由ai-service的消费者处理
 *
 * @author wenliangchang
 */
@Component
@Slf4j
public class InterviewCreateMessageStrategy implements BenefitMessageStrategy {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendBusinessMessage(String messageId, Long businessId, Long accountId) {
        // 构建消息DTO
        InterviewCreateMessageDTO messageDTO = InterviewCreateMessageDTO.builder()
                .interviewId(businessId)
                .accountId(accountId)
                .messageId(messageId)
                .build();

        // 发送到面试创建队列
        rabbitTemplate.convertAndSend(
                InterviewEventMQConfig.INTERVIEW_EVENT_EXCHANGE,
                InterviewEventMQConfig.INTERVIEW_CREATE_ROUTING_KEY,
                messageDTO
        );

        log.info("发送面试创建业务消息成功, messageId={}, interviewId={}", messageId, businessId);
    }

    @Override
    public String getStrategyType() {
        // 支持所有面试类型
        return "INTERVIEW";
    }
}
