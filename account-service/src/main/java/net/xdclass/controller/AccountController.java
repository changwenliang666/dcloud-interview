package net.xdclass.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.xdclass.dto.SendCheckCodeDTO;
import net.xdclass.service.AccountService;
import net.xdclass.util.CaptchaUtil;
import net.xdclass.util.JsonData;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final CaptchaUtil captchaUtil;
    @Resource
    private AccountService accountService;

    public AccountController(CaptchaUtil captchaUtil) {
        this.captchaUtil = captchaUtil;
    }

    /**
     * 获取图形验证码
     */
    @GetMapping(value = "/get-captcha",produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getCaptcha(@Parameter(description = "手机号或邮箱", required = true) @RequestParam(value="identifier") String identifier, @Parameter(description = "验证码类型:login-登录,register-注册", required = true) @RequestParam(value="type") String type ) {
        return captchaUtil.generateCaptchaImage(identifier,type);
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/send-check-code")
    public JsonData sendCheckCode(@Valid @RequestBody SendCheckCodeDTO req) {
        return accountService.sendCheckCode(req);
    }
}
