package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.RedisKeyManager;
import net.xdclass.config.WechatLoginConfig;
import net.xdclass.enums.AccountRoleEnum;
import net.xdclass.enums.StatusEnum;
import net.xdclass.util.*;
import net.xdclass.dto.AccountDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.WechatQrCodeSceneStatusEnum;
import net.xdclass.exception.BizException;
import net.xdclass.mapper.AccountMapper;
import net.xdclass.model.AccountDO;
import net.xdclass.service.WechatService;
import net.xdclass.dto.AccountLoginResultVO;
import net.xdclass.dto.WechatAccountVO;
import net.xdclass.dto.WechatQrCodeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatServiceImpl implements WechatService {

    @Autowired
    private WechatLoginConfig wechatLoginConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 重建 access_token 的锁过期时间。请求正常结束会主动删除；进程异常退出后靠过期时间释放
     */
    private static final long ACCESS_TOKEN_LOCK_EXPIRE_SECONDS = 10L;

    /**
     * 未抢到锁时的重试次数
     */
    private static final int ACCESS_TOKEN_LOCK_RETRY_TIMES = 2;

    /**
     * 未抢到锁时的重试间隔
     */
    private static final long ACCESS_TOKEN_LOCK_WAIT_MILLIS = 200L;

    /**
     * 缓存提前失效时间，避免 token 临界过期仍被使用
     */
    private static final int ACCESS_TOKEN_EXPIRE_AHEAD_SECONDS = 300;
    @Autowired
    private AccountMapper accountMapper;

    /**
     * 获取微信 access_token。
     * 缓存未命中时用分布式锁重建，避免高并发同时请求微信接口。
     */
    @Override
    public String getAccessToken() {
        String cacheKey = String.format(RedisKeyManager.WECHAT_ACCESS_TOKEN, wechatLoginConfig.getAppId());
        String cachedToken = getCachedAccessToken(cacheKey);
        if (cachedToken != null) {
            return cachedToken;
        }
        return rebuildAccessToken(cacheKey);
    }

    private String getCachedAccessToken(String cacheKey) {
        String cachedToken = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cachedToken == null || cachedToken.isEmpty()) {
            return null;
        }
        log.info("从缓存获取access_token成功");
        return cachedToken;
    }

    /**
     * 抢锁成功的请求回源微信并写缓存，其余请求等待后读缓存。
     */
    private String rebuildAccessToken(String cacheKey) {
        String lockKey = String.format(RedisKeyManager.WECHAT_ACCESS_TOKEN_LOCK, wechatLoginConfig.getAppId());
        for (int i = 0; i < ACCESS_TOKEN_LOCK_RETRY_TIMES; i++) {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                    lockKey, "1", ACCESS_TOKEN_LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                try {
                    String cachedToken = getCachedAccessToken(cacheKey);
                    if (cachedToken != null) {
                        return cachedToken;
                    }
                    log.info("获取access_token分布式锁成功，开始重建缓存, appId:{}", wechatLoginConfig.getAppId());
                    return requestAndCacheAccessToken(cacheKey);
                } finally {
                    redisTemplate.delete(lockKey);
                }
            }
            if (!waitForAccessTokenRebuild()) {
                return null;
            }
            String cachedToken = getCachedAccessToken(cacheKey);
            if (cachedToken != null) {
                return cachedToken;
            }
        }
        log.warn("重建access_token获取分布式锁超时, appId:{}", wechatLoginConfig.getAppId());
        return null;
    }

    private boolean waitForAccessTokenRebuild() {
        try {
            TimeUnit.MILLISECONDS.sleep(ACCESS_TOKEN_LOCK_WAIT_MILLIS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("等待access_token缓存重建被中断");
            return false;
        }
    }

    private String requestAndCacheAccessToken(String cacheKey) {
        try {
            String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                    wechatLoginConfig.getAccessTokenUrl(),
                    wechatLoginConfig.getAppId(),
                    wechatLoginConfig.getAppSecret());

            log.info("请求微信access_token, appId:{}", wechatLoginConfig.getAppId());
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                log.error("获取微信access_token失败，响应为空, appId:{}", wechatLoginConfig.getAppId());
                return null;
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("errcode")) {
                log.error("获取微信access_token失败, appId:{}, errmsg:{}",
                        wechatLoginConfig.getAppId(), jsonNode.get("errmsg").asText());
                return null;
            }

            String accessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            long cacheSeconds = expiresIn - ACCESS_TOKEN_EXPIRE_AHEAD_SECONDS;
            if (accessToken == null || accessToken.isEmpty() || cacheSeconds <= 0) {
                log.error("微信access_token响应异常, appId:{}, expiresIn:{}", wechatLoginConfig.getAppId(), expiresIn);
                return null;
            }

            redisTemplate.opsForValue().set(cacheKey, accessToken, cacheSeconds, TimeUnit.SECONDS);
            log.info("重建access_token缓存成功, appId:{}, cacheSeconds:{}", wechatLoginConfig.getAppId(), cacheSeconds);
            return accessToken;
        } catch (Exception e) {
            log.error("获取微信access_token失败, appId:{}", wechatLoginConfig.getAppId(), e);
            return null;
        }
    }

    // 更新微信登录二维码状态
    private void updateWechatQrCodeStatus(String status, String sceneId) {
        try {
            String redisKey = String.format(RedisKeyManager.WECHAT_QR_CODE_STATUS, sceneId);
            WechatQrCodeVO wechatQrCodeVO = (WechatQrCodeVO) redisTemplate.opsForValue().get(redisKey);
            if (wechatQrCodeVO == null) {
                throw new BizException(BizCodeEnum.WECHAT_CODE_EXPIRED);
            }
            wechatQrCodeVO.setStatus(status);
            redisTemplate.opsForValue().set(redisKey, wechatQrCodeVO, wechatLoginConfig.getQrCodeExpireTime(), TimeUnit.SECONDS);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("status参数不合法");
        } catch (Exception e) {
            throw new BizException(BizCodeEnum.WECHAT_CODE_UPDATE_FAIL, e);
        }
    }

    // 根据openId 查询微信用户详细信息
    private WechatAccountVO getWechatAccountInfo(String openId) {
        try {
            String accessToken = getAccessToken();
            String url = String.format("%s?access_token=%s&openid=%s", wechatLoginConfig.getUserInfoUrl(), accessToken, openId);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.has("errcode")) {
                log.error("获取微信用户信息失败：{}", jsonNode.get("errmsg"));
                throw new BizException(BizCodeEnum.WECHAT_ACCOUNT_GET_FAIL);
            }
            return WechatAccountVO.builder()
                    .openid(jsonNode.get("openid").asText())
                    .unionid(jsonNode.has("unionid") ? jsonNode.get("unionid").asText() : "")
                    .subscribe(jsonNode.get("subscribe").asInt())
                    .subscribeTime(jsonNode.get("subscribe_time").asLong())
                    .remark(jsonNode.get("remark").asText())
                    .groupid(jsonNode.get("groupid").asInt()).build();

        } catch (Exception e) {
            throw new BizException(BizCodeEnum.WECHAT_ACCOUNT_GET_FAIL);
        }
    }

    // 扫码的微信用户是登录还是注册
    private AccountLoginResultVO registryOrLogin(WechatAccountVO wechatAccountVO) {
        AccountDO accountDO = accountMapper.selectOne(new LambdaQueryWrapper<AccountDO>().eq(AccountDO::getOpenid, wechatAccountVO.getOpenid()));
        if (accountDO == null) {
            accountDO = AccountDO.builder().username("wx_" + CommonUtil.generateUUID())
                    .openid(wechatAccountVO.getOpenid())
                    .unionid(wechatAccountVO.getUnionid())
                    .status(StatusEnum.ON.name())
                    .role(AccountRoleEnum.COMMON.name())
                    .build();
            accountMapper.insert(accountDO);
            log.info("微信用户注册成功:openId:{}", accountDO.getOpenid());
        }
        AccountDTO accountDTO = SpringBeanUtil.copyProperties(accountDO, AccountDTO.class);
        String token = JwtUtil.geneTenantAccountLoginJWT(accountDTO);
        return AccountLoginResultVO.builder().token(token).success(true).accountDTO(accountDTO).build();
    }

    public String createQrCodeTicket(String accessToken, String sceneId) {
        //构建请求URL
        try {
            String url = String.format("%s?access_token=%s", wechatLoginConfig.getQrCodeUrl(), accessToken);

            //构建请求体
            Map<String, Object> body = Map.of(
                    "action_name", "QR_STR_SCENE",
                    "expire_seconds", wechatLoginConfig.getQrCodeExpireTime(),
                    "action_info", Map.of("scene", Map.of("scene_str", sceneId))
            );

            String jsonBody = JsonUtil.obj2Json(body);
            log.info("请求微信服务器, url:{}, body:{}", url, jsonBody);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, jsonBody, String.class);
            String responseBody = responseEntity.getBody();
            log.info("微信服务器返回:{}", responseBody);

            //解析结果
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("ticket")) {
                String ticket = jsonNode.get("ticket").asText();
                log.info("创建二维码成功：{}", ticket);
                String qrCodeUrl = String.format("%s?ticket=%s", wechatLoginConfig.getShowQrCodeUrl(), ticket);
                log.info("二维码地址：{}", qrCodeUrl);
                return qrCodeUrl;
            } else {
                log.error("创建二维码失败：{}", jsonNode.get("errmsg"));
                return null;
            }

        } catch (Exception e) {
            log.error("创建二维码异常", e);
        }
        return null;
    }

    @Override
    public JsonData generateQrCode() {
        // 生成场景id
        String sceneId = CommonUtil.generateUUID();
        String accessToken = getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            log.error("获取access_token失败，sceneId：{}", sceneId);
            return JsonData.buildError("获取access_token失败，请检查微信API配置");
        }
        log.info("获取access_token成功，sceneId：{}", sceneId);

        String qrCodeUrl = createQrCodeTicket(accessToken, sceneId);
        if (qrCodeUrl == null || qrCodeUrl.isEmpty()) {
            log.error("创建二维码ticket失败，sceneId：{}", sceneId);
            return JsonData.buildError("创建二维码失败，请检查微信API配置");
        }
        log.info("创建二维码ticket成功，sceneId：{}", sceneId);
        WechatQrCodeVO wechatQrCodeVO = WechatQrCodeVO.builder()
                .sceneId(sceneId)
                .qrCodeUrl(qrCodeUrl)
                .expireTime(wechatLoginConfig.getQrCodeExpireTime())
                .status(WechatQrCodeSceneStatusEnum.WAITING.name())
                .createTime(System.currentTimeMillis()).build();
        String redisKey = String.format(RedisKeyManager.WECHAT_QR_CODE_STATUS, sceneId);
        redisTemplate.opsForValue().set(redisKey, wechatQrCodeVO, wechatLoginConfig.getQrCodeExpireTime(), TimeUnit.SECONDS);
        log.info("生成微信登录二维码成功，sceneId：{}，qrCodeUrl：{}", sceneId, qrCodeUrl);
        return JsonData.buildSuccess(wechatQrCodeVO);
    }

    @Override
    public JsonData handleEventCallback(String openId, String sceneId) {
        try {
            String qrcodeRedisKey = String.format(RedisKeyManager.WECHAT_QR_CODE_STATUS, sceneId);
            WechatQrCodeVO wechatQrCodeVO = (WechatQrCodeVO) redisTemplate.opsForValue().get(qrcodeRedisKey);
            // 检查微信二维码是否过期
            if (wechatQrCodeVO == null) { // 过期
                return JsonData.buildCodeAndMsg(BizCodeEnum.WECHAT_CODE_EXPIRED.getCode(), BizCodeEnum.WECHAT_CODE_EXPIRED.getMessage());
            }
            //1.更新二维码状态为SCAN
            updateWechatQrCodeStatus(WechatQrCodeSceneStatusEnum.SCANNED.name(), sceneId);
            //2.获取用户信息
            WechatAccountVO wechatAccountVO = getWechatAccountInfo(openId);
            //3.判断是登录还是注册
            AccountLoginResultVO accountLoginResultVO = registryOrLogin(wechatAccountVO);
            //4.更新二维码状态，将用户信息放入到redis中，让前端查询
            updateWechatQrCodeStatus(WechatQrCodeSceneStatusEnum.CONFIRMED.name(), sceneId);
            String userInfoRedisKey = String.format(RedisKeyManager.WECHAT_LOGIN_RESULT, sceneId);
            redisTemplate.opsForValue().set(userInfoRedisKey, accountLoginResultVO, wechatLoginConfig.getLoginStateExpireTime(),TimeUnit.SECONDS);
            return JsonData.buildSuccess(accountLoginResultVO);
        } catch (Exception e) {
            log.error("微信扫码登录异常:{}", e.getMessage());
            return JsonData.buildError("微信扫码登录失败");
        }
    }

    @Override
    public JsonData getWechatUserScanResult(String sceneId) {
        String redisQrcodeKey = String.format(RedisKeyManager.WECHAT_QR_CODE_STATUS,sceneId);
        WechatQrCodeVO wechatQrCodeVO = (WechatQrCodeVO) redisTemplate.opsForValue().get(redisQrcodeKey);
        AccountLoginResultVO accountLoginResultVO = null;
        if(wechatQrCodeVO != null) {
            if(wechatQrCodeVO.getStatus().equals(WechatQrCodeSceneStatusEnum.CONFIRMED.name())) {
                String redisLoginResultKey = String.format(RedisKeyManager.WECHAT_LOGIN_RESULT,sceneId);
                accountLoginResultVO  = (AccountLoginResultVO) redisTemplate.opsForValue().get(redisLoginResultKey);
                if(accountLoginResultVO == null) {
                    return JsonData.buildCodeAndMsg(BizCodeEnum.WECHAT_CODE_LOGIN_RESULT_FAIL.getCode(),BizCodeEnum.WECHAT_CODE_LOGIN_RESULT_FAIL.getMessage());
                }
            }
        }else {
            return JsonData.buildCodeAndMsg(BizCodeEnum.WECHAT_CODE_EXPIRED.getCode(), BizCodeEnum.WECHAT_CODE_EXPIRED.getMessage());
        }

        return JsonData.buildSuccess(accountLoginResultVO);
    }
}