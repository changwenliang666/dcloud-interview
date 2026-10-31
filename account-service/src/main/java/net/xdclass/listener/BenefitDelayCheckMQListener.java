package net.xdclass.listener;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.BenefitDelayCheckMQConfig;
import net.xdclass.dto.BenefitDelayCheckMessageDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.exception.BizException;
import net.xdclass.service.BenefitTaskService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 权益延迟检查MQ消费者
 * 监听延迟检查队列，检查业务是否完成，必要时回滚权益
 *
 * 设计说明：
 * - 由account-service负责延迟检查
 * - 检查业务是否完成（通过Feign调用ai-service）
 * - 如果业务未完成，回滚权益
 */
@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = BenefitDelayCheckMQConfig.BENEFIT_DELAY_CHECK_CONSUME_QUEUE)
public class BenefitDelayCheckMQListener {

    private static final int MAX_QUERY_RETRY = 2;

    private final BenefitTaskService benefitTaskService;

    private final RabbitTemplate rabbitTemplate;

    @RabbitHandler
    public void handleDelayCheck(BenefitDelayCheckMessageDTO message, Message amqpMessage, Channel channel) {
        log.info("监听到延迟检查消息, messageId={}, businessId={}, checkLevel={}",
                message.getMessageId(), message.getBusinessId(), message.getCheckLevel());

        try {
            benefitTaskService.checkAndCompensate(message.getMessageId(), message.getCheckLevel());
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            log.info("延迟检查消费成功, messageId={}", message.getMessageId());
        } catch (BizException e) {
            if (e.getCode() == BizCodeEnum.SYSTEM_ERROR.getCode()) {
                retryOrCompensate(message, amqpMessage, channel, e);
                return;
            }
            reject(message, amqpMessage, channel, e);
        } catch (Exception e) {
            reject(message, amqpMessage, channel, e);
        }
    }

    /**
     * 查不到简历状态时延后重试。重试耗尽仍无法确认，退回次数，避免权益一直锁定。
     */
    private void retryOrCompensate(BenefitDelayCheckMessageDTO message, Message amqpMessage, Channel channel, Exception cause) {
        int retryCount = message.getRetryCount() == null ? 0 : message.getRetryCount();
        log.error("延迟检查未确认简历状态, messageId={}, retryCount={}", message.getMessageId(), retryCount, cause);
        try {
            if (retryCount >= MAX_QUERY_RETRY) {
                benefitTaskService.compensateWhenStatusUnknown(message.getMessageId());
                channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
                log.error("延迟检查重试耗尽, messageId={}", message.getMessageId());
                return;
            }
            message.setRetryCount(retryCount + 1);
            rabbitTemplate.convertAndSend(BenefitDelayCheckMQConfig.BENEFIT_DELAY_CHECK_EXCHANGE,
                    BenefitDelayCheckMQConfig.DELAY_CHECK_ROUTING_KEY_1MIN, message);
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            log.info("延迟检查已安排重试, messageId={}, retryCount={}", message.getMessageId(), message.getRetryCount());
        } catch (Exception ex) {
            log.error("延迟检查重试失败, messageId={}", message.getMessageId(), ex);
            rejectQuietly(amqpMessage, channel);
        }
    }

    private void reject(BenefitDelayCheckMessageDTO message, Message amqpMessage, Channel channel, Exception cause) {
        log.error("延迟检查消费失败, messageId={}", message.getMessageId(), cause);
        rejectQuietly(amqpMessage, channel);
    }

    private void rejectQuietly(Message amqpMessage, Channel channel) {
        try {
            channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
        } catch (Exception ex) {
            log.error("消息拒绝失败", ex);
        }
    }
}