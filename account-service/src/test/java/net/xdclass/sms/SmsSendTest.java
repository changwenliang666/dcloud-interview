package net.xdclass.sms;

import net.xdclass.util.HttpUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 国阳云短信发送案例
 * <p>
 * 调用前把 {@link #APP_CODE} 和 {@link #MOBILE} 换成自己的 AppCode 与手机号。
 * 签名和模板使用国阳云公开测试 ID，正文为：验证码：**code**，**minute**分钟内有效。
 */
public class SmsSendTest {

    private static final Logger log = LoggerFactory.getLogger(SmsSendTest.class);

    private static final String SMS_URL = "https://gyytz.market.alicloudapi.com/sms/smsSend";

    /**
     * 阿里云市场 AppCode
     */
    private static final String APP_CODE = "c3811e9cf80e4834869ad22d9642540f";

    /**
     * 接收短信的手机号
     */
    private static final String MOBILE = "18814798162";

    /**
     * 测试签名 ID。可在国阳云控制台申请，说明见 http://help.guoyangyun.com/Problem/Qm.html
     */
    private static final String SMS_SIGN_ID = "2e65b1bb3d054466b82f0c9d125465e2";

    /**
     * 测试模板 ID
     */
    private static final String TEMPLATE_ID = "908e94ccf08b4476ba6c876d13f084ad";

    @Test
    public void TestSendSms() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + APP_CODE);

        Map<String, Object> params = new HashMap<>();
        params.put("mobile", MOBILE);
        params.put("param", "**code**:12345 ,**minute**:5");
        params.put("smsSignId", SMS_SIGN_ID);
        params.put("templateId", TEMPLATE_ID);

        // 国阳云示例把业务参数放在 Query，请求体为空
        String requestUrl = cn.hutool.http.HttpUtil.urlWithForm(SMS_URL, params, StandardCharsets.UTF_8, true);
        String result = HttpUtil.postForm(requestUrl, headers, new HashMap<>());
        log.info("短信发送响应: {}", result);
        Assert.assertNotNull("短信发送失败", result);
    }
}
