package net.xdclass.client;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.dto.SendSmsDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.util.HttpUtil;
import net.xdclass.util.JsonData;
import net.xdclass.util.JsonUtil;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SmsClient {
    /**
     * 短信服务商地址
     */
    private static final String SMS_URL = "https://gyytz.market.alicloudapi.com/sms/smsSend";
    /**
     * 阿里云市场 AppCode
     */
    private static final String APP_CODE = "c3811e9cf80e4834869ad22d9642540f";
    /**
     * 测试签名 ID
     */
    private static final String SMS_SIGN_ID = "2e65b1bb3d054466b82f0c9d125465e2";
    /**
     * 测试模板 ID
     */
    private static final String TEMPLATE_ID = "908e94ccf08b4476ba6c876d13f084ad";

    /**
     * 短信验证码过期时间 10分钟
     */
    private static final Integer EXPIRE_TIME = 10;

    public JsonData sendSms(SendSmsDTO req) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + APP_CODE);
        Map<String, Object> params = new HashMap<>();
        params.put("mobile", req.getMobile());
        String param = String.format("**code**:%s,**minute**:%d", req.getCode(), EXPIRE_TIME);
        params.put("param", param);
        params.put("smsSignId", SMS_SIGN_ID);
        params.put("templateId", TEMPLATE_ID);

        try {
            // 国阳云示例把业务参数放在 Query，请求体为空
            String requestUrl = cn.hutool.http.HttpUtil.urlWithForm(SMS_URL, params, StandardCharsets.UTF_8, true);
            String result = HttpUtil.postForm(requestUrl, headers, new HashMap<>());
            Map resultStatus = JsonUtil.json2Obj(result, Map.class);
            return resultStatus.get("code").equals("0") ?
                    JsonData.buildSuccess("短信验证码发送成功") :
                    JsonData.buildCodeAndMsg(BizCodeEnum.CODE_SEND_FAIL.getCode(), BizCodeEnum.CODE_SEND_FAIL.getMessage());
        } catch (Exception e) {
            log.error("短信发送失败:" + e.getMessage());
            return JsonData.buildCodeAndMsg(BizCodeEnum.CODE_SEND_FAIL.getCode(), BizCodeEnum.CODE_SEND_FAIL.getMessage());
        }
    }
}
