package net.xdclass.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.WechatLoginConfig;
import net.xdclass.service.WechatService;
import net.xdclass.util.JsonData;
import net.xdclass.util.WechatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wechat")
@Tag(name = "微信回调", description = "微信服务器回调接口")
public class WechatController {

    @Autowired
    private final WechatLoginConfig wechatLoginConfig;

    @Autowired
    private WechatService wechatService;

    /**
     * 验证微信服务器签名
     */
    private boolean verifySignature(String signature, String timestamp, String nonce) {
        try {
            // 1. 将token、timestamp、nonce三个参数进行字典序排序
            String[] arr = {wechatLoginConfig.getToken(), timestamp, nonce};
            Arrays.sort(arr);

            // 2. 将三个参数字符串拼接成一个字符串进行sha1加密
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(s);
            }

            // 3. 进行SHA1加密
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes());

            // 4. 将加密后的字节数组转换为16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // 5. 将计算出的签名与微信发送的signature对比
            String calculatedSignature = hexString.toString();
            boolean isValid = calculatedSignature.equals(signature);

            log.info("签名验证：计算签名={}, 接收签名={}, 验证结果={}",
                    calculatedSignature, signature, isValid);

            return isValid;

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-1算法不支持", e);
            return false;
        } catch (Exception e) {
            log.error("签名验证失败", e);
            return false;
        }
    }

    /**
     * 微信服务器验证接口
     * 用于验证微信服务器
     */
    @GetMapping("/callback/verify")
    @Operation(summary = "微信服务器验证", description = "微信服务器验证接口")
    public String verifyWechatServer(
            @RequestParam("signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {

        log.info("微信服务器验证请求：signature={}, timestamp={}, nonce={}, echostr={}",
                signature, timestamp, nonce, echostr);

        // 验证签名
        if (verifySignature(signature, timestamp, nonce)) {
            log.info("微信服务器验证成功");
            return echostr;
        } else {
            log.error("微信服务器验证失败");
            return "error";
        }
    }

    /**
     * 微信扫码回调接口
     *
     * @return
     */

    @PostMapping("/callback/verify")
    public void verifyWechatScanCallBack(HttpServletRequest request) {
        try {
            // 从wechat 扫码回调中拿到参数
            String requestBody = WechatUtil.readRequestBody(request);
            Map<String, String> eventData = WechatUtil.parseXmlData(requestBody);
            String event = eventData.get("Event");
            String sceneId = eventData.get("EventKey");
            String openId = eventData.get("FromUserName");
            log.info("微信扫码事件,openId:{},sceneId:{}", openId, sceneId);

            switch (event) {
                case "SCAN":
                    log.info("处理扫码事件");
                    wechatService.handleEventCallback(openId,sceneId);
                    break;
                case "subscribe":
                    log.info("处理关注事件");
                    if (sceneId != null && sceneId.startsWith("qrscene_")) {
                        sceneId = sceneId.substring(8);
                        wechatService.handleEventCallback(openId,sceneId);
                    }
                    break;
                case "unsubscribe":
                    log.info("处理取消关注事件");
                    break;
                default:
                    log.info("处理未知事件");
                    break;
            }
        } catch (Exception e) {
            log.error("微信扫码异常:{}", e.getMessage());
        }
    }

    @GetMapping("/get-access-token")
    public JsonData getAccessToken() {
        String accessToken = wechatService.getAccessToken();
        return JsonData.buildSuccess(accessToken);
    }

    @GetMapping("/qrcode")
    public JsonData generateQrCode() {
        return wechatService.generateQrCode();
    }

    @GetMapping("/get-wechat-user-scan-result")
    public JsonData getWechatUserScanResult(@RequestParam("sceneId") String sceneId) {
        return wechatService.getWechatUserScanResult(sceneId);
    }
}