package net.xdclass.biz;

import com.wechat.pay.java.service.payments.model.Transaction;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.WechatPayConfig;
import net.xdclass.util.WechatPayUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WechatPayUtilTest {


    /**
     * 读取 .env 内容
     */
    @BeforeAll
    static void loadEnv() {
        System.out.println("开始测试");
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources")
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
        log.info("=== .env 加载完成，注入 {} 个环境变量 ===", dotenv.entries().size());
    }


    @Autowired
    private WechatPayUtil wechatPayUtil;

    @Autowired
    private WechatPayConfig wechatPayConfig;


    /** 同一批测试共用同一个订单号，便于下单→查询→关闭流转 */
    private static final String OUT_TRADE_NO = "changwenliang12138"  ;

    /** 测试金额：1 分（最小单位），避免真实扣款 */
    private static final Long AMOUNT_FEN = 1L;

    private static final String DESCRIPTION = "WechatPayUtil集成测试订单";

    @Test
    @Order(1)
    @DisplayName("测试配置文件加载")
    void testConfigLoaded(){
        log.info("商户号：{}", wechatPayConfig.getMchId());
        log.info("商户密钥：{}", wechatPayConfig.getApiV3Key());
        log.info("商户证书序列号：{}", wechatPayConfig.getCertSerialNo());
        log.info("商户证书路径：{}", wechatPayConfig.getPrivateKeyAbsolutePath());
        log.info("微信公钥路径：{}", wechatPayConfig.getPublicKeyAbsolutePath());

        //加断言
        assertThat(wechatPayConfig.getMchId()).isNotBlank();
        assertThat(wechatPayConfig.getApiV3Key()).isNotBlank();
        assertThat(wechatPayConfig.getCertSerialNo()).isNotBlank();
        assertThat(wechatPayConfig.getPrivateKeyAbsolutePath()).isNotBlank();
    }

    /**
     * 测试创建订单
     */
    @Test
    @Order(2)
    @DisplayName("测试创建微信支付订单")
    void testNativePayOrder(){
        log.info("测试创建微信支付订单");
        String nativePayOrder = wechatPayUtil.createNativePayOrder(OUT_TRADE_NO, AMOUNT_FEN, DESCRIPTION);
        log.info("微信支付下单成功，codeUrl:{}", nativePayOrder);
    }

    /**
     * 测试查询订单
     */
    @Test
    @Order(3)
    @DisplayName("测试查询微信支付订单")
    void testQueryOrder(){
        log.info("测试查询微信支付订单");
        Transaction transaction = wechatPayUtil.queryOrderByOutTradeNo(OUT_TRADE_NO);
        log.info("查询订单支付状态:{}", transaction.getTradeState());
        assertThat(transaction.getTradeState()).isEqualTo(Transaction.TradeStateEnum.NOTPAY);
    }

    /**
     * 关闭订单
     */
    @Test
    @Order(4)
    @DisplayName("测试关闭微信支付订单")
    void testCloseOrder(){
        log.info("测试关闭微信支付订单");
        boolean closeOrder = wechatPayUtil.closeOrder(OUT_TRADE_NO);
        log.info("关闭订单成功:{}", closeOrder);
        assertThat(closeOrder).isTrue();
    }

    /**
     * 关闭订单再查询
     */
    @Test
    @Order(5)
    @DisplayName("测试关闭微信支付订单后再查询")
    void testQueryOrderAfterClose(){
        log.info("测试关闭微信支付订单后再查询");
        Transaction transaction = wechatPayUtil.queryOrderByOutTradeNo(OUT_TRADE_NO);
        log.info("查询订单支付状态:{}", transaction.getTradeState());
        assertThat(transaction.getTradeState()).isEqualTo(Transaction.TradeStateEnum.CLOSED);
    }
}
