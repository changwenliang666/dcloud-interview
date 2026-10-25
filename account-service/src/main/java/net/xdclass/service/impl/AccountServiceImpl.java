package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.client.SmsClient;
import net.xdclass.config.RedisKeyManager;
import net.xdclass.controller.req.AccountLoginReq;
import net.xdclass.controller.req.SendCheckCodeReq;
import net.xdclass.dto.AccountDTO;
import net.xdclass.dto.AccountLoginResultVO;
import net.xdclass.dto.AccountVO;
import net.xdclass.dto.SmsDTO;
import net.xdclass.enums.AccountRoleEnum;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.StatusEnum;
import net.xdclass.exception.BizException;
import net.xdclass.interceptor.AccountLoginInterceptor;
import net.xdclass.mapper.AccountMapper;
import net.xdclass.model.AccountDO;
import net.xdclass.service.AccountService;
import net.xdclass.service.FileService;
import net.xdclass.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
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
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private FileService fileService;

    // 短信验证码过期时间
    private final Integer CODE_EXPIRED_TIME = 10;

    // 校验短信验证码
    private boolean verifyIdentifierCode(String identifier, String checkCode, String type) {
        String cacheKey = String.format(RedisKeyManager.CHECK_CODE_KEY, type, identifier);
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.isNotBlank(cacheValue)) {
            String[] parts = cacheValue.split("_");
            String cacheCode = parts[0];
            if (cacheCode.equals(checkCode)) {
//                redisTemplate.delete(cacheKey);
                return true;
            }
        }

        return false;
    }

    @Override
    public JsonData sendCheckCode(SendCheckCodeReq req) {
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
                if (nowTime - sendCodeTime < 1000 * 60) {
                    return JsonData.buildCodeAndMsg(BizCodeEnum.CODE_LIMITED.getCode(), BizCodeEnum.CODE_LIMITED.getMessage());
                }
            }
        } catch (DataAccessException e) {
            log.error("验证码缓存读取失败, key:{}", redisKey, e);
            return JsonData.buildError("短信服务异常，请联系管理员！");
        }

        // 生成短信验证码
        String numberCode = CommonUtil.generateNumberCode();
        SmsDTO sendSmsDTO = SmsDTO.builder().code(numberCode).mobile(req.getIdentifier()).build();

        JsonData sendResult = smsClient.sendSms(sendSmsDTO);
        if (sendResult.getCode() == 0) {
            long nowSendTime = System.currentTimeMillis();
            String redis_cache = numberCode + "_" + nowSendTime;
            try {
                // 短信发送成功，保存到redis中
                redisTemplate.opsForValue().set(redisKey, redis_cache, CODE_EXPIRED_TIME, TimeUnit.MINUTES);
            } catch (DataAccessException e) {
                log.error("验证码缓存写入失败, key:{}", redisKey, e);
                return JsonData.buildError("短信服务异常，请联系管理员！");
            }
        }
        return sendResult;
    }

    @Override
    public AccountLoginResultVO login(AccountLoginReq req) {
        // 校验短信验证码是否存在
        boolean verifyCode = verifyIdentifierCode(req.getIdentifier(), req.getCheckCode(), req.getType());
        if (!verifyCode) {
            throw new BizException(BizCodeEnum.SMS_CODE_ERROR);
        }
        AccountDO accountDO = accountMapper.selectOne(new LambdaQueryWrapper<AccountDO>()
                .eq(AccountDO::getStatus, StatusEnum.ON.name())
                .and(w ->
                        w.eq(AccountDO::getEmail, req.getIdentifier())
                        .or()
                        .eq(AccountDO::getPhone, req.getIdentifier()))
        );
        // 用户不存在，走注册流程
        if(accountDO == null) {
            boolean isPhone =  CommonUtil.isPhone(req.getIdentifier());
            accountDO = AccountDO.builder().role(AccountRoleEnum.COMMON.name()).username("user_" + req.getIdentifier()).build();
            if(isPhone){
                accountDO.setPhone(req.getIdentifier());
            }else {
                accountDO.setEmail(req.getIdentifier());
            }
            accountMapper.insert(accountDO);
            log.info("用户注册成功：{}", accountDO);
        }

        AccountDTO accountDTO = SpringBeanUtil.copyProperties(accountDO, AccountDTO.class);

        //生成token
        String loginJWT = JwtUtil.geneTenantAccountLoginJWT(accountDTO);

        return AccountLoginResultVO.builder().success(true).token(loginJWT).accountDTO(accountDTO).build();
    }

    @Override
    public List<AccountVO> getAllUser(String status) {
        return accountMapper.selectList(new LambdaQueryWrapper<AccountDO>().eq(AccountDO::getStatus, status))
                .stream()
                .map(account -> SpringBeanUtil.copyProperties(account, AccountVO.class))
                .toList();
    }

    @Override
    public JsonData uploadAvatar(MultipartFile file) {
        //获取当前用户
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        Long accountId = accountDTO.getId();

        //调用文件上传
        JsonData uploadResult = fileService.uploadPublicFile(file);
        if(uploadResult.getCode() != 0){
            log.error("文件上传失败：{}", uploadResult);
            return uploadResult;
        }
        //获取上传后的的文件地址
        Map<String,Object> resultData = (Map<String,Object>)uploadResult.getData();
        String fileUrl = resultData.get("url").toString();
        log.info("文件上传成功，文件地址：{}", fileUrl);

        //更新数据库
        int updated = accountMapper.update(AccountDO.builder().headImg(fileUrl).build(), new LambdaUpdateWrapper<AccountDO>().eq(AccountDO::getId, accountId));
        if(updated <= 0){
            log.error("更新用户头像失败，用户ID：{}", accountId);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }

        log.info("更新用户头像成功，用户ID：{}", accountId);
        return JsonData.buildSuccess(fileUrl);
    }
}
