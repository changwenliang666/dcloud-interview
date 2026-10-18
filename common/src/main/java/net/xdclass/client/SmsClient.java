package net.xdclass.client;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.dto.SmsDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.util.HttpUtil;
import net.xdclass.util.JsonData;
import net.xdclass.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SmsClient {
    /**
     * 短信服务商地址
     */
    @Value("${sms.send-url}")
    private String SEND_URL;
    /**
     * 阿里云市场 AppCode
     */
    @Value("${sms.app-code}")
    private String APP_CODE;
    /**
     * 测试签名 ID
     */
    @Value("${sms.sms-sign-id}")
    private String SMS_SIGN_ID;
    /**
     * 测试模板 ID
     */
    @Value("${sms.template-id}")
    private String TEMPLATE_ID;

    /**
     * 短信验证码过期时间 10分钟
     */
    private static final Integer EXPIRE_TIME = 10;

    public JsonData sendSms(SmsDTO req) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + APP_CODE);
        Map<String, Object> params = new HashMap<>();
        params.put("mobile", req.getMobile());
        String param = String.format("**code**:%s,**minute**:%d", req.getCode(), EXPIRE_TIME);
        params.put("param", param);
        params.put("smsSignId", SMS_SIGN_ID);
        params.put("templateId", TEMPLATE_ID);

        try {
            // 国阳云示例把业务参数放在 Query，并用 URLEncoder 编码。Hutool 不会编码 *，网关会返回 403
            String requestUrl = buildRequestUrl(params);
            String result = HttpUtil.postForm(requestUrl, headers, new HashMap<>());
            if (result == null) {
                log.error("短信发送失败, 短信网关无成功响应");
                return buildSendFail();
            }
            Map resultStatus = JsonUtil.json2Obj(result, Map.class);
            if (resultStatus == null || !"0".equals(String.valueOf(resultStatus.get("code")))) {
                log.error("短信发送失败, result:{}", result);
                return buildSendFail();
            }
            return JsonData.buildSuccess("短信验证码发送成功");
        } catch (Exception e) {
            log.error("短信发送失败", e);
            return buildSendFail();
        }
    }

    /**
     * 按阿里云市场示例拼接 Query，参数值使用 application/x-www-form-urlencoded 编码。
     */
    private String buildRequestUrl(Map<String, Object> params) {
        StringBuilder query = new StringBuilder();
        params.forEach((key, value) -> {
            if (query.length() > 0) {
                query.append('&');
            }
            query.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8));
        });
        return SEND_URL + "?" + query;
    }

    private JsonData buildSendFail() {
        return JsonData.buildCodeAndMsg(BizCodeEnum.CODE_SEND_FAIL.getCode(), BizCodeEnum.CODE_SEND_FAIL.getMessage());
    }
}
