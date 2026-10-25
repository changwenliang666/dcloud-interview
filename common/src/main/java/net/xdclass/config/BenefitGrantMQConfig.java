package net.xdclass.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 权益发放 MQ 配置（common 模块共享）
 * 同时提供全局 JSON 消息转换器，供 product-service 和 account-service 使用
 */
@Configuration
@Data
public class BenefitGrantMQConfig {

    // 权益发放交换机
    public static final String BENEFIT_GRANT_EXCHANGE = "benefit.grant.exchange";

    // 权益发放队列（account-service 消费）
    public static final String BENEFIT_GRANT_QUEUE = "benefit.grant.queue";

    // 权益发放路由 key
    public static final String BENEFIT_GRANT_ROUTING_KEY = "benefit.grant.routing.key";

    /**
     * 配置 JSON 消息转换器，替代默认的 Java 原生序列化
     * Spring AMQP 3.x 默认使用 Java 原生序列化，会校验反序列化白名单导致失败，
     * 改用 Jackson2JsonMessageConverter 以 JSON 方式序列化/反序列化消息
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate，使用 JSON 消息转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange benefitGrantExchange() {
        return new TopicExchange(BENEFIT_GRANT_EXCHANGE, true, false);
    }

    @Bean
    public Queue benefitGrantQueue() {
        return new Queue(BENEFIT_GRANT_QUEUE, true, false, false);
    }

    @Bean
    public Binding benefitGrantBinding() {
        return BindingBuilder.bind(benefitGrantQueue())
                .to(benefitGrantExchange())
                .with(BENEFIT_GRANT_ROUTING_KEY);
    }
}