package net.xdclass.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.client.SmsClient;
import net.xdclass.config.RedisKeyManager;
import net.xdclass.dto.SendCheckCodeDTO;
import net.xdclass.dto.SendSmsDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.service.AccountService;
import net.xdclass.util.CaptchaUtil;
import net.xdclass.util.CommonUtil;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Autowired
    private CaptchaUtil captchaUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SmsClient smsClient;

    // 短信验证码过期时间
    private final Integer CODE_EXPIRED_TIME = 10;

    @Override
    public JsonData sendCheckCode(SendCheckCodeDTO req) {
        // 校验图形验证码是否正确
        try {
            captchaUtil.verifyCaptcha(req.getIdentifier(), req.getCaptcha(), req.getType());
        } catch (RuntimeException e) { // 图形验证码过期或者错误
            return JsonData.buildCodeAndMsg(BizCodeEnum.CODE_CAPTCHA_EXPIRED.getCode(), BizCodeEnum.CODE_CAPTCHA_EXPIRED.getMessage());
        }

        String redisKey = String.format(RedisKeyManager.CHECK_CODE_KEY, req.getType(), req.getIdentifier());
        try {
            // 从redis中判断是否已经生成了验证码
            String checkCode = redisTemplate.opsForValue().get(redisKey);
            if (checkCode != null && !checkCode.isEmpty()) {
                String[] args = checkCode.split("_");
                long sendCodeTime = Long.parseLong(args[1]);
                long nowTime = System.currentTimeMillis();
                // 发送间隔小于60s
                if(nowTime - sendCodeTime < 1000 * 60) {
                    return JsonData.buildCodeAndMsg(BizCodeEnum.CODE_LIMITED.getCode(),BizCodeEnum.CODE_LIMITED.getMessage());
                }
            }
        }catch (DataAccessException e) {
            log.error("验证码缓存读取失败, key:{}", redisKey, e);
            return JsonData.buildError("短信服务异常，请联系管理员！");
        }

        // 生成短信验证码
        String numberCode =  CommonUtil.generateNumberCode();
        SendSmsDTO sendSmsDTO =  SendSmsDTO.builder().code(numberCode).mobile(req.getIdentifier()).build();

        JsonData sendResult =  smsClient.sendSms(sendSmsDTO);
        if(sendResult.getCode() == 0) {
            long nowSendTime = System.currentTimeMillis();
            String redis_cache = numberCode + "_" + nowSendTime;
            try {
                // 短信发送成功，保存到redis中
                redisTemplate.opsForValue().set(redisKey,redis_cache,CODE_EXPIRED_TIME, TimeUnit.MINUTES);
            }catch (DataAccessException e) {
                log.error("验证码缓存写入失败, key:{}", redisKey, e);
                return JsonData.buildError("短信服务异常，请联系管理员！");
            }
        }
        return sendResult;
    }
}
