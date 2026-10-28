package net.xdclass.strategy;

/**
 * 权益消息发送策略接口
 * 用于ai-service发送不同类型的业务MQ消息
 *
 * 设计说明：
 * - 策略接口定义在common模块，便于共享
 * - 策略实现在ai-service，与业务逻辑内聚
 * - account-service只负责权益扣减和延迟检查，不发送业务MQ
 */
public interface BenefitMessageStrategy {

    /**
     * 发送业务消息
     * 由ai-service在权益扣减成功后调用
     *
     * @param messageId 消息ID（任务ID，用于关联延迟检查）
     * @param businessId 业务ID（简历ID/面试ID）
     * @param accountId 账号ID
     */
    void sendBusinessMessage(String messageId, Long businessId, Long accountId);

    /**
     * 获取策略类型（对应权益编码）
     *
     * @return 权益编码
     */
    String getStrategyType();
}