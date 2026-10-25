package net.xdclass.listener;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.BenefitGrantMQConfig;
import net.xdclass.dto.BenefitGrantMessage;
import net.xdclass.service.AccountBenefitService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = BenefitGrantMQConfig.BENEFIT_GRANT_QUEUE)
public class BenefitGrantMQListener {

    private final AccountBenefitService accountBenefitService;

    @RabbitHandler
    public void benefitGrantHandler(BenefitGrantMessage message, Message amqpMessage, Channel channel) {
        log.info("监听到权益发放消息, outTradeNo:{}, accountId:{}, orderType:{}, 权益数量:{}",
                message.getOutTradeNo(), message.getAccountId(), message.getOrderType(),
                message.getBenefitItems() != null ? message.getBenefitItems().size() : 0);
        try {
            // 核心：调用权益发放服务处理业务逻辑
            accountBenefitService.grantBenefit(message);
            log.info("权益发放消费成功, outTradeNo:{}", message.getOutTradeNo());
            // 手动 ACK：告知 RabbitMQ 消息已处理成功
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("权益发放消费失败, outTradeNo:{}", message.getOutTradeNo(), e);
            try {
                // NACK + requeue=false：消息不重新入队，避免无限循环
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, false);
            } catch (Exception ex) {
                log.error("消息拒绝失败", ex);
            }
        }
    }
}