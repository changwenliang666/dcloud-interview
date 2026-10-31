package net.xdclass.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 权益延迟检查MQ配置
 * 用于分布式事务的延迟检查机制
 *
 * 设计说明：
 * - account-service负责发送延迟检查消息
 * - 延迟检查消息用于检查业务是否完成，必要时回滚权益
 *
 */
@Configuration
public class BenefitDelayCheckMQConfig {

    // ==================== 延迟检查交换机 ====================
    public static final String BENEFIT_DELAY_CHECK_EXCHANGE = "benefit.delay.check.exchange";

    // ==================== 延迟检查队列（1分钟） ====================
    public static final String BENEFIT_DELAY_CHECK_QUEUE_1MIN = "benefit.delay.check.queue.1min";

    // ==================== 延迟检查队列（5分钟） ====================
    public static final String BENEFIT_DELAY_CHECK_QUEUE_5MIN = "benefit.delay.check.queue.5min";

    // ==================== 延迟检查队列（Serverless 接收后 8 分钟） ====================
    public static final String BENEFIT_DELAY_CHECK_QUEUE_8MIN = "benefit.delay.check.queue.8min";

    // ==================== 延迟检查队列（扣次后 20 分钟，绝对超时） ====================
    public static final String BENEFIT_DELAY_CHECK_QUEUE_20MIN = "benefit.delay.check.queue.20min";

    // ==================== 延迟检查消费队列 ====================
    public static final String BENEFIT_DELAY_CHECK_CONSUME_QUEUE = "benefit.delay.check.consume.queue";

    // ==================== 路由Key ====================
    public static final String DELAY_CHECK_ROUTING_KEY_1MIN = "benefit.delay.check.1min";
    public static final String DELAY_CHECK_ROUTING_KEY_5MIN = "benefit.delay.check.5min";
    public static final String DELAY_CHECK_ROUTING_KEY_8MIN = "benefit.delay.check.8min";
    public static final String DELAY_CHECK_ROUTING_KEY_20MIN = "benefit.delay.check.20min";
    public static final String DELAY_CHECK_CONSUME_ROUTING_KEY = "benefit.delay.check.consume";

    // ==================== TTL（毫秒） ====================
    private static final Integer TTL_1_MIN = 1000 * 60;
    private static final Integer TTL_5_MIN = 1000 * 60 * 5;
    private static final Integer TTL_8_MIN = 1000 * 60 * 8;
    private static final Integer TTL_20_MIN = 1000 * 60 * 20;

    /**
     * 延迟检查交换机
     */
    @Bean
    public TopicExchange benefitDelayCheckExchange() {
        return new TopicExchange(BENEFIT_DELAY_CHECK_EXCHANGE, true, false);
    }

    /**
     * 延迟检查队列（1分钟）
     * 使用死信队列实现延迟消息
     */
    @Bean
    public Queue benefitDelayCheckQueue1Min() {
        Map<String, Object> args = new HashMap<>(3);
        // 消息过期后转发的交换机
        args.put("x-dead-letter-exchange", BENEFIT_DELAY_CHECK_EXCHANGE);
        // 消息过期后转发的路由key
        args.put("x-dead-letter-routing-key", DELAY_CHECK_CONSUME_ROUTING_KEY);
        // 消息过期时间（1分钟）
        args.put("x-message-ttl", TTL_1_MIN);
        return new Queue(BENEFIT_DELAY_CHECK_QUEUE_1MIN, true, false, false, args);
    }

    /**
     * 延迟检查队列（5分钟）
     * 使用死信队列实现延迟消息
     */
    @Bean
    public Queue benefitDelayCheckQueue5Min() {
        Map<String, Object> args = new HashMap<>(3);
        // 消息过期后转发的交换机
        args.put("x-dead-letter-exchange", BENEFIT_DELAY_CHECK_EXCHANGE);
        // 消息过期后转发的路由key
        args.put("x-dead-letter-routing-key", DELAY_CHECK_CONSUME_ROUTING_KEY);
        // 消息过期时间（5分钟）
        args.put("x-message-ttl", TTL_5_MIN);
        return new Queue(BENEFIT_DELAY_CHECK_QUEUE_5MIN, true, false, false, args);
    }

    /**
     * Serverless 已接收后的超时检查。函数执行时间应短于这 8 分钟。
     */
    @Bean
    public Queue benefitDelayCheckQueue8Min() {
        Map<String, Object> args = new HashMap<>(3);
        args.put("x-dead-letter-exchange", BENEFIT_DELAY_CHECK_EXCHANGE);
        args.put("x-dead-letter-routing-key", DELAY_CHECK_CONSUME_ROUTING_KEY);
        args.put("x-message-ttl", TTL_8_MIN);
        return new Queue(BENEFIT_DELAY_CHECK_QUEUE_8MIN, true, false, false, args);
    }

    /**
     * 扣次后的绝对超时。覆盖排队过久、接收回执丢失和回调丢失。
     */
    @Bean
    public Queue benefitDelayCheckQueue20Min() {
        Map<String, Object> args = new HashMap<>(3);
        args.put("x-dead-letter-exchange", BENEFIT_DELAY_CHECK_EXCHANGE);
        args.put("x-dead-letter-routing-key", DELAY_CHECK_CONSUME_ROUTING_KEY);
        args.put("x-message-ttl", TTL_20_MIN);
        return new Queue(BENEFIT_DELAY_CHECK_QUEUE_20MIN, true, false, false, args);
    }

    /**
     * 延迟检查消费队列（死信队列，被消费者监听）
     */
    @Bean
    public Queue benefitDelayCheckConsumeQueue() {
        return new Queue(BENEFIT_DELAY_CHECK_CONSUME_QUEUE, true, false, false);
    }

    /**
     * 延迟检查队列绑定（1分钟）
     */
    @Bean
    public Binding benefitDelayCheckBinding1Min() {
        return BindingBuilder.bind(benefitDelayCheckQueue1Min())
                .to(benefitDelayCheckExchange())
                .with(DELAY_CHECK_ROUTING_KEY_1MIN);
    }

    /**
     * 延迟检查队列绑定（5分钟）
     */
    @Bean
    public Binding benefitDelayCheckBinding5Min() {
        return BindingBuilder.bind(benefitDelayCheckQueue5Min())
                .to(benefitDelayCheckExchange())
                .with(DELAY_CHECK_ROUTING_KEY_5MIN);
    }

    @Bean
    public Binding benefitDelayCheckBinding8Min() {
        return BindingBuilder.bind(benefitDelayCheckQueue8Min())
                .to(benefitDelayCheckExchange())
                .with(DELAY_CHECK_ROUTING_KEY_8MIN);
    }

    @Bean
    public Binding benefitDelayCheckBinding20Min() {
        return BindingBuilder.bind(benefitDelayCheckQueue20Min())
                .to(benefitDelayCheckExchange())
                .with(DELAY_CHECK_ROUTING_KEY_20MIN);
    }

    /**
     * 延迟检查消费队列绑定
     */
    @Bean
    public Binding benefitDelayCheckConsumeBinding() {
        return BindingBuilder.bind(benefitDelayCheckConsumeQueue())
                .to(benefitDelayCheckExchange())
                .with(DELAY_CHECK_CONSUME_ROUTING_KEY);
    }
}
