package net.xdclass.util;

import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.WechatPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WechatPayUtil {


    @Autowired
    private WechatPayConfig wechatPayConfig;

    /**
     * 公钥模式配置
     */
    private RSAPublicKeyConfig config;

    /**
     * native支付服务
     */
    private NativePayService nativePayService;

    /**
     * 回调通知解释器
     */
    private NotificationParser notificationParser;


    /**
     * Bean初始化创建微信支付所需对象
     */
    @PostConstruct
    public void init() {
        config = new RSAPublicKeyConfig.Builder()
                .merchantId(wechatPayConfig.getMchId())
                .privateKeyFromPath(wechatPayConfig.getPrivateKeyAbsolutePath())
                .publicKeyFromPath(wechatPayConfig.getPublicKeyAbsolutePath())
                .publicKeyId(wechatPayConfig.getPublicKeyId())
                .merchantSerialNumber(wechatPayConfig.getCertSerialNo())
                .apiV3Key(wechatPayConfig.getApiV3Key())
                .build();
        nativePayService = new NativePayService.Builder().config(config).build();
        notificationParser = new NotificationParser(config);
        log.info("初始化微信支付服务完成");

    }


    /**
     * Native支付下单接口，获取二维码
     */
    public String createNativePayOrder(String outTradeNo, Long amount,String description) {

        PrepayRequest request = new PrepayRequest();
        request.setAppid(wechatPayConfig.getAppId());
        request.setMchid(wechatPayConfig.getMchId());
        request.setDescription(description);
        request.setOutTradeNo(outTradeNo);
        request.setNotifyUrl(wechatPayConfig.getNotifyUrl());

        Amount reqAmount = new Amount();
        reqAmount.setTotal(amount.intValue());
        request.setAmount(reqAmount);
        log.info("创建微信支付订单参数:{}",request);
        PrepayResponse response = nativePayService.prepay(request);
        String codeUrl = response.getCodeUrl();
        log.info("微信支付下单成功，codeUrl:{}",codeUrl);
        return codeUrl;
    }


    /**
     * 查询订单支付状态
     */
    public Transaction queryOrderByOutTradeNo(String outTradeNo) {
        try {

            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(wechatPayConfig.getMchId());
            request.setOutTradeNo(outTradeNo);
            Transaction transaction = nativePayService.queryOrderByOutTradeNo(request);
            log.info("查询订单支付状态:{}",transaction);
            return transaction;
        }catch (Exception e){
            log.error("查询订单支付状态异常:{}",e);
            return null;
        }
    }

    /**
     * 关闭订单接口
     */
    public boolean closeOrder(String outTradeNo) {
        try {
            CloseOrderRequest request = new CloseOrderRequest();
            request.setMchid(wechatPayConfig.getMchId());
            request.setOutTradeNo(outTradeNo);
            nativePayService.closeOrder(request);
            log.info("关闭订单成功");
            return true;
        }catch (Exception e){
            log.error("关闭订单异常:{}",e);
            return false;
        }
    }
}
