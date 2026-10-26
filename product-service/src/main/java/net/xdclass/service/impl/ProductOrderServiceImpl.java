package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.BenefitGrantMQConfig;
import net.xdclass.config.ProductOrderMQConfig;
import net.xdclass.controller.req.ProductBenefitOrderReq;
import net.xdclass.controller.req.ProductOrderPageReq;
import net.xdclass.controller.req.ProductPackageOrderReq;
import net.xdclass.enums.*;
import net.xdclass.interceptor.AccountLoginInterceptor;
import net.xdclass.mapper.BenefitMapper;
import net.xdclass.mapper.ProductOrderMapper;
import net.xdclass.model.BenefitDO;
import net.xdclass.model.ProductOrderDO;
import net.xdclass.service.ProductService;
import net.xdclass.util.CommonUtil;
import net.xdclass.util.JsonData;
import net.xdclass.util.SpringBeanUtil;
import net.xdclass.util.WechatPayUtil;
import net.xdclass.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ProductOrderServiceImpl implements net.xdclass.service.ProductOrderService {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductOrderMapper productOrderMapper;
    @Autowired
    private WechatPayUtil wechatPayUtil;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ProductOrderMQConfig productOrderMQConfig;
    @Autowired
    private BenefitMapper benefitMapper;


    // 获取账号id
    private Long getCurrentAccountId() {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        return accountDTO.getId();
    }

    /**
     * 根据支付方式路由到对应的支付逻辑
     */
    private JsonData handlePayment(ProductOrderDO orderDO, String payType) {
        ProductOrderVO productOrderVO = SpringBeanUtil.copyProperties(orderDO, ProductOrderVO.class);
        // 投递延迟消息
        rabbitTemplate.convertAndSend(productOrderMQConfig.getOrderEventExchange(), productOrderMQConfig.getOrderCloseDelayRoutingKey(), productOrderVO);
        if (PaymentEnum.WECHAT_PAY.name().equals(payType)) {
            //微信支付
            return doWechatNativePay(orderDO);
        } else if (PaymentEnum.ALI_PAY.name().equals(payType)) {
            return JsonData.buildError("支付宝支付暂时未实现");
        }
        return JsonData.buildError("不支持的支付方式");
    }

    /**
     * 发起Native支付,构建支付信息
     */
    private JsonData doWechatNativePay(ProductOrderDO orderDO) {
        long payAmountFen = orderDO.getPayAmount().multiply(BigDecimal.valueOf(100)).longValue();
        String codeUrl = wechatPayUtil.createNativePayOrder(orderDO.getOutTradeNo(), payAmountFen, orderDO.getTitle());
        if (StringUtils.isBlank(codeUrl)) {
            log.error("微信支付下单失败");
            return JsonData.buildError("微信支付下单失败");
        }
        PayInfoVO payInfoVO = PayInfoVO.builder()
                .outTradeNo(orderDO.getOutTradeNo())
                .payAmount(payAmountFen)
                .payType(PaymentEnum.WECHAT_PAY.name())
                .codeUrl(codeUrl).build();

        log.info("微信支付下单成功，返回参数:{}", payInfoVO);
        return JsonData.buildSuccess(payInfoVO);
    }

    private ProductOrderVO selectByOutTradeNo(String outTradeNo) {
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(
                new LambdaQueryWrapper<ProductOrderDO>()
                        .eq(ProductOrderDO::getOutTradeNo, outTradeNo)
        );
        return productOrderDO == null ? null : SpringBeanUtil.copyProperties(productOrderDO, ProductOrderVO.class);
    }

    /**
     * 更新订单状态为指定状态，带幂等检查和数据库级 CAS 保障
     */
    private Integer updateOrderState(String outTradeNo, String transactionNo, String newState) {
        ProductOrderVO orderVO = selectByOutTradeNo(outTradeNo);
        if (orderVO == null) {
            log.warn("订单不存在，无法更新状态，订单号：{}", outTradeNo);
            return 0;
        }
        if (newState.equals(orderVO.getOrderState())) {
            log.info("订单{}已是{}状态，跳过", outTradeNo, newState);
            return 0;  // 已是目标状态，视为成功（幂等）
        }

        // 数据库级 CAS：避免并发回调或超时任务同时更新导致的状态覆盖
        int updated = productOrderMapper.update(null,
                new LambdaUpdateWrapper<ProductOrderDO>()
                        .set(ProductOrderDO::getTransactionNo, transactionNo)
                        .set(ProductOrderDO::getOrderState, newState)
                        .set(OrderStateEnum.PAY.name().equals(newState), ProductOrderDO::getNotifyTime, new Date())
                        .eq(ProductOrderDO::getOutTradeNo, outTradeNo)
                        .eq(ProductOrderDO::getOrderState, OrderStateEnum.NEW.name()));

        // 排除目标状态，防止重复更新

        if (updated > 0) {
            log.info("订单状态已更新，订单号：{}，新状态：{}", outTradeNo, newState);
            return updated;
        } else {
            log.warn("订单状态更新失败（可能已被其他线程更新），订单号：{}，目标状态：{}", outTradeNo, newState);
            return updated;
        }
    }

    /**
     * 构建微信回调失败响应
     */
    private String buildFailResponse(String message) {
        return "{\"code\":\"FAIL\",\"message\":\"" + message + "\"}";
    }

    private static final String WECHAT_CALLBACK_SUCCESS = "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";

    @Override
    public JsonData createBenefitOrder(ProductBenefitOrderReq req) {
        //生成订单编号
        String outTradeNo = String.format("%s%s%s", OrderIdPrefixEnum.BFT.name(), CommonUtil.getCurrentTimestamp(), CommonUtil.generateStringNumRandom(6));
        try {
            // 获取权益信息
            BenefitVO benefitVO = productService.getBenefitById(req.getId());
            if (benefitVO == null || benefitVO.getStatus().equals(StatusEnum.OFF.name())) {
                return JsonData.buildCodeAndMsg(BizCodeEnum.BENEFIT_NOT_EXIST.getCode(), BizCodeEnum.BENEFIT_NOT_EXIST.getMessage());
            }

            BigDecimal payAmount = benefitVO.getUnitPrice().multiply(new BigDecimal(req.getPurchaseCount())).multiply((req.getDiscount()));
            ProductOrderDO productOrderDO = ProductOrderDO.builder()
                    .outTradeNo(outTradeNo)
                    .accountId(getCurrentAccountId())
                    .orderType(OrderTypeEnum.BENEFIT_ORDER.name())
                    .title(benefitVO.getName())
                    .benefitId(benefitVO.getId())
                    .purchaseCount(req.getPurchaseCount())
                    .unitPrice(benefitVO.getUnitPrice())
                    .discount(req.getDiscount())
                    .payAmount(payAmount)
                    .orderState(OrderStateEnum.NEW.name())
                    .payType(req.getPaymentType())
                    .build();
            int rows = productOrderMapper.insert(productOrderDO);
            if (rows < 1) {
                log.error("创建订单失败:数据库插入失败");
                return JsonData.buildCodeAndMsg(BizCodeEnum.ORDER_ADD_FAIL.getCode(), BizCodeEnum.ORDER_ADD_FAIL.getMessage());
            }

            return handlePayment(productOrderDO, req.getPaymentType());
        } catch (Exception e) {
            log.error("创建订单异常", e);
            return JsonData.buildCodeAndMsg(BizCodeEnum.ORDER_ADD_FAIL.getCode(), BizCodeEnum.ORDER_ADD_FAIL.getMessage());
        }
    }

    @Override
    public JsonData createPackageOrder(ProductPackageOrderReq req) {
        //生成订单编号
        String outTradeNo = String.format("%s%s%s", OrderIdPrefixEnum.PKG.name(), CommonUtil.getCurrentTimestamp(), CommonUtil.generateStringNumRandom(6));
        try {
            ProductPackageVO productPackageVO = productService.getPackageById(req.getId());
            if (productPackageVO == null) {
                return JsonData.buildCodeAndMsg(BizCodeEnum.PACKAGE_NOT_EXIST.getCode(), BizCodeEnum.PRODUCT_NOT_EXIST.getMessage());
            }

            BigDecimal payAmount = productPackageVO.getPrice().multiply(new BigDecimal(req.getPurchaseCount())).multiply((req.getDiscount()));
            ProductOrderDO productOrderDO = ProductOrderDO.builder()
                    .outTradeNo(outTradeNo)
                    .accountId(getCurrentAccountId())
                    .orderType(OrderTypeEnum.PACKAGE_ORDER.name())
                    .title(productPackageVO.getName())
                    .productPackageId(productPackageVO.getId())
                    .purchaseCount(req.getPurchaseCount())
                    .unitPrice(productPackageVO.getPrice())
                    .discount(req.getDiscount())
                    .payAmount(payAmount)
                    .orderState(OrderStateEnum.NEW.name())
                    .payType(req.getPaymentType())
                    .build();
            int rows = productOrderMapper.insert(productOrderDO);
            if (rows < 1) {
                log.error("创建订单失败:数据库插入失败");
                return JsonData.buildCodeAndMsg(BizCodeEnum.ORDER_ADD_FAIL.getCode(), BizCodeEnum.ORDER_ADD_FAIL.getMessage());
            }

            return handlePayment(productOrderDO, req.getPaymentType());
        } catch (Exception e) {
            log.error("创建订单异常", e);
            return JsonData.buildCodeAndMsg(BizCodeEnum.ORDER_ADD_FAIL.getCode(), BizCodeEnum.ORDER_ADD_FAIL.getMessage());
        }
    }

    @Override
    public ProductOrderVO getOrderStatusByOutTradeNo(String outTradeNo) {
        LambdaQueryWrapper<ProductOrderDO> queryWrapper = new LambdaQueryWrapper<ProductOrderDO>().eq(ProductOrderDO::getOutTradeNo, outTradeNo).eq(ProductOrderDO::getAccountId, getCurrentAccountId());
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(queryWrapper);
        return productOrderDO != null ? SpringBeanUtil.copyProperties(productOrderDO, ProductOrderVO.class) : null;
    }

    @Override
    public Map<String, Object> getOrderListByPage(ProductOrderPageReq req) {
        Long accountId = getCurrentAccountId();
        LambdaQueryWrapper<ProductOrderDO> queryWrapper = new LambdaQueryWrapper<ProductOrderDO>()
                .eq(ProductOrderDO::getAccountId, accountId)
                .eq(StringUtils.isNotBlank(req.getOrderType()), ProductOrderDO::getOrderType, req.getOrderType())
                .eq(StringUtils.isNotBlank(req.getPayType()), ProductOrderDO::getPayType, req.getPayType())
                .eq(StringUtils.isNotBlank(req.getOrderState()), ProductOrderDO::getOrderState, req.getOrderState())
                .orderByDesc(ProductOrderDO::getGmtCreate);

        Page<ProductOrderDO> page = new Page<>(req.getPageIndex(), req.getPageNum());
        Page<ProductOrderDO> resultPage = productOrderMapper.selectPage(page, queryWrapper);
        List<ProductOrderVO> orderList = SpringBeanUtil.copyProperties(resultPage.getRecords(), ProductOrderVO.class);
        log.info("分页查询订单成功, accountId:{}, total:{}", accountId, resultPage.getTotal());
        return CommonUtil.convertToPageMap(resultPage, orderList);
    }

    @Override
    public ResponseEntity<String> handleWechatPayCallback(String timestamp, String nonce, String signature, String signType, String serialNumber, String body) {

        Transaction transaction = wechatPayUtil.parseCallbackNotification(timestamp, nonce, signature, signType, serialNumber, body);
        if (transaction == null) {
            log.error("解析微信回调异常");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildFailResponse("解析微信回调异常"));
        }

        String outTradeNo = transaction.getOutTradeNo();
        Transaction.TradeStateEnum tradeState = transaction.getTradeState();
        log.info("微信回调, outTradeNo:{}, tradeState:{}", outTradeNo, tradeState);
        ProductOrderVO productOrderVO = selectByOutTradeNo(outTradeNo);
        if (productOrderVO == null) {
            log.error("订单不存在, outTradeNo:{}", outTradeNo);
            //验证微信回调通过，不返回任何内容
            return ResponseEntity.ok(WECHAT_CALLBACK_SUCCESS);
        }

        if (Transaction.TradeStateEnum.SUCCESS.equals(tradeState)) {
            //检查数据库是否已经更新
            if (productOrderVO.getOrderState().equals(OrderStateEnum.PAY.name())) {
                log.warn("订单已支付, outTradeNo:{}", outTradeNo);
                return ResponseEntity.ok(WECHAT_CALLBACK_SUCCESS);
            }
            //更新数据库
            Integer updated = updateOrderState(outTradeNo, transaction.getTransactionId(), OrderStateEnum.PAY.name());
            if (updated > 0) {
                // 发放权益
                processOrderBenefits(outTradeNo);
                log.info("更新订单成功, outTradeNo:{}", outTradeNo);
            } else {
                log.warn("更新订单失败, outTradeNo:{}", outTradeNo);
            }

        } else if (!OrderStateEnum.PAY.name().equals(productOrderVO.getOrderState())) {
            Integer updated = updateOrderState(outTradeNo, transaction.getTransactionId(), OrderStateEnum.CANCEL.name());
            if (updated < 1) {
                log.warn("更新订单失败，冲突或者不存在, outTradeNo:{}", outTradeNo);
            } else {
                log.info("订单未支付成功, outTradeNo:{}", outTradeNo);
            }
        }

        return ResponseEntity.ok(WECHAT_CALLBACK_SUCCESS);
    }

    /**
     * 消息队列延迟消息，检查订单状态
     */
    @Override
    public void handleTimeoutOrder(ProductOrderVO productOrderVO) {
        String outTradeNo = productOrderVO.getOutTradeNo();
        try {
            ProductOrderVO productOrderDO = selectByOutTradeNo(outTradeNo);
            // 订单不存在
            if (productOrderDO == null) {
                log.info("[订单延迟队列消息]：订单不存在，订单编号:{}", outTradeNo);
            } else if (OrderStateEnum.PAY.name().equals(productOrderDO.getOrderState())) {
                log.info("[订单延迟队列消息]：订单已完成支付，订单编号:{}", outTradeNo);
            } else if (OrderStateEnum.CANCEL.name().equals(productOrderDO.getOrderState())) {
                log.info("[订单延迟队列消息]：订单已取消支付，订单编号:{}", outTradeNo);
            } else {
                // 支付方式为微信支付
                if (productOrderDO.getPayType().equals(PaymentEnum.WECHAT_PAY.name())) {
                    // 调用微信侧，查询订单的状态
                    Transaction transaction = wechatPayUtil.queryOrderByOutTradeNo(outTradeNo);
                    Transaction.TradeStateEnum tradeState = transaction.getTradeState();

                    // 已支付
                    if (Transaction.TradeStateEnum.SUCCESS.equals(tradeState)) {
                        int rows = updateOrderState(outTradeNo, transaction.getTransactionId(), OrderStateEnum.PAY.name());
                        if (rows < 1) {
                            log.error("[订单延迟队列消息]：更新订单状态为已支付失败，订单编号:{}", outTradeNo);
                        } else {
                            //发送发放权益消息
                            processOrderBenefits(productOrderDO.getOutTradeNo());
                            log.info("[订单延迟队列消息]：更新订单状态为已支付成功，订单编号:{}", outTradeNo);
                        }
                    } else if (Transaction.TradeStateEnum.USERPAYING.equals(tradeState)) {
                        log.info("[订单延迟队列消息]：用户正在支付中，订单编号:{}", outTradeNo);
                    } else { // 已关闭
                        if (wechatPayUtil.closeOrder(outTradeNo)) {
                            int rows = updateOrderState(outTradeNo, transaction.getTransactionId(), OrderStateEnum.CANCEL.name());
                            if (rows < 1) {
                                log.error("[订单延迟队列消息]：更新订单状态为已关闭失败，订单编号:{}", outTradeNo);
                            } else {
                                log.info("[订单延迟队列消息]：更新订单状态为已关闭成功，订单编号:{}", outTradeNo);
                            }
                        } else {
                            log.error("[订单延迟队列消息]：更新订单状态为已关闭失败，订单编号:{}", outTradeNo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[订单延迟队列消息]：延迟更新订单状态失败，订单编号：{}", outTradeNo, e);
        }
    }

    /**
     * 单权益发放
     **/
    private void processSingleBenefit(ProductOrderDO orderDO) {
        BenefitDO benefitDO = benefitMapper.selectById(orderDO.getBenefitId());
        try {
            // 单权益发送对象实例
            BenefitGrantMessage.BenefitItem benefitItem = BenefitGrantMessage.BenefitItem
                    .builder()
                    .benefitId(benefitDO.getId())
                    .benefitCode(benefitDO.getBenefitCode())
                    .count(orderDO.getPurchaseCount())
                    .validDays(benefitDO.getValidDays())
                    .build();
            BenefitGrantMessage benefitGrantMessage = BenefitGrantMessage
                    .builder()
                    .outTradeNo(orderDO.getOutTradeNo())
                    .accountId(orderDO.getAccountId())
                    .orderId(orderDO.getId())
                    .orderType(orderDO.getOrderType())
                    .benefitItems(List.of(benefitItem))
                    .build();


            rabbitTemplate.convertAndSend(BenefitGrantMQConfig.BENEFIT_GRANT_EXCHANGE, BenefitGrantMQConfig.BENEFIT_GRANT_ROUTING_KEY, benefitGrantMessage);
            log.info("单权益发放消息已发送, outTradeNo:{}, benefitId:{}，交换机:{},路由:{}", orderDO.getOutTradeNo(), benefitDO.getId(),BenefitGrantMQConfig.BENEFIT_GRANT_EXCHANGE,BenefitGrantMQConfig.BENEFIT_GRANT_ROUTING_KEY);

        } catch (Exception e) {
            log.error("单权益发放消息发送失败, outTradeNo:{}, benefitId:{}", orderDO.getOutTradeNo(), benefitDO.getId(),e);
        }
    }

    /**
     * 套餐发放
     */
    private void processPackageBenefit(ProductOrderDO orderDO) {
        Long productPackageId = orderDO.getProductPackageId();
        ProductPackageVO productPackageVO = productService.getPackageById(productPackageId);
        if (productPackageVO == null) {
            log.error("套餐权益发放失败，该套餐不存在，订单编号:{}, 套餐id:{}", orderDO.getOutTradeNo(), productPackageId);
            return;
        }
        // 获取当前套餐下的所有权益
        List<PackageBenefitVO> benefitVOList = productPackageVO.getBenefitList();

        if (benefitVOList.isEmpty()) {
            log.error("套餐权益发放失败，该套餐下无权益，订单编号:{}, 套餐id:{}", orderDO.getOutTradeNo(), productPackageId);
            return;
        }

        List<BenefitGrantMessage.BenefitItem> benefitItems = new ArrayList<>();

        for (PackageBenefitVO benefitVO : benefitVOList) {
            BenefitGrantMessage.BenefitItem benefitItem = BenefitGrantMessage.BenefitItem
                    .builder()
                    .benefitId(benefitVO.getId())
                    .benefitCode(benefitVO.getBenefitCode())
                    .count(orderDO.getPurchaseCount())
                    .validDays(benefitVO.getValidDays())
                    .build();
            benefitItems.add(benefitItem);
        }

        BenefitGrantMessage benefitGrantMessage = BenefitGrantMessage
                .builder()
                .outTradeNo(orderDO.getOutTradeNo())
                .accountId(orderDO.getAccountId())
                .orderId(orderDO.getId())
                .orderType(orderDO.getOrderType())
                .benefitItems(benefitItems)
                .build();

        rabbitTemplate.convertAndSend(BenefitGrantMQConfig.BENEFIT_GRANT_EXCHANGE, BenefitGrantMQConfig.BENEFIT_GRANT_ROUTING_KEY, benefitGrantMessage);
        log.info("套餐权益发放消息已经发送，订单编号:{},套餐id:{}", orderDO.getOutTradeNo(), orderDO.getProductPackageId());
    }

    /**
     * 发放权益统一入口
     *
     * @param outTradeNo 订单编号
     */

    private void processOrderBenefits(String outTradeNo) {
        try {
            LambdaQueryWrapper<ProductOrderDO> queryWrapper = new LambdaQueryWrapper<ProductOrderDO>().eq(ProductOrderDO::getOutTradeNo, outTradeNo);
            ProductOrderDO productOrderDO = productOrderMapper.selectOne(queryWrapper);
            if (productOrderDO == null) {
                log.error("权益发放消息发送失败，该订单不存在,订单编号:{}", outTradeNo);
                return;
            }

            // 没订阅不发放权益
            if (!productOrderDO.getOrderState().equals(OrderStateEnum.PAY.name())) {
                log.info("该订单未支付，订单编号:{}", outTradeNo);
                return;
            }

            if (OrderTypeEnum.BENEFIT_ORDER.name().equals(productOrderDO.getOrderType())) { // 单权益发放
                processSingleBenefit(productOrderDO);
            } else if (OrderTypeEnum.PACKAGE_ORDER.name().equals(productOrderDO.getOrderType())) { // 套餐发放
                processPackageBenefit(productOrderDO);
            }
        } catch (Exception e) {
            log.error("发放权益消息发送失败，订单编号:{}",outTradeNo);
        }
    }
}
