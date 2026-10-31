package net.xdclass.listener;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.InterviewEventMQConfig;
import net.xdclass.dto.ResumeAnalyseMessageDTO;
import net.xdclass.service.impl.ResumeAnalyseDispatchService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消费简历分析消息，异步触发 Serverless。确认消息发生在平台返回已接收之后。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = InterviewEventMQConfig.RESUME_ANALYSE_QUEUE, ackMode = "MANUAL", concurrency = "1")
public class ResumeAnalyseMQListener {

    private final ResumeAnalyseDispatchService resumeAnalyseDispatchService;

    @RabbitHandler
    public void handle(ResumeAnalyseMessageDTO message, Message amqpMessage, Channel channel) {
        boolean redelivered = Boolean.TRUE.equals(amqpMessage.getMessageProperties().getRedelivered());
        log.info("监听到简历分析消息, resumeId={}, messageId={}, redelivered={}",
                message == null ? null : message.getResumeId(),
                message == null ? null : message.getMessageId(),
                redelivered);
        try {
            String result = resumeAnalyseDispatchService.dispatch(message, redelivered);
            if (ResumeAnalyseDispatchService.DROP.equals(result)) {
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
                log.warn("简历分析投递失败，丢弃本次消息，等待延迟检查补发, messageId={}",
                        message == null ? null : message.getMessageId());
                return;
            }
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("简历分析消息消费失败, messageId={}", message == null ? null : message.getMessageId(), e);
            try {
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                log.error("消息拒绝失败", ex);
            }
        }
    }
}
