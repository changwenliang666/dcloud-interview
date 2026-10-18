package net.xdclass.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.xdclass.controller.rq.AccountLoginReq;
import net.xdclass.controller.rq.SendCheckCodeRq;
import net.xdclass.dto.AccountLoginResultDTO;
import net.xdclass.service.AccountService;
import net.xdclass.util.CaptchaUtil;
import net.xdclass.util.JsonData;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public JsonData sendCheckCode(@Valid @RequestBody SendCheckCodeRq req) {
        return accountService.sendCheckCode(req);
    }

    /**
     * 用户登录
      */

    @PostMapping("/login")
    public JsonData login(@Valid @RequestBody AccountLoginReq req) {
        AccountLoginResultDTO loginResult = accountService.login(req);
        return JsonData.buildSuccess(loginResult);
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/get-all-user")
    public JsonData getAllUser(@RequestParam("status") String status) {
        return JsonData.buildSuccess(accountService.getAllUser(status));
    }

    /**
     * 上传头像 公开访问
     */
    @PostMapping(value = "/avatar",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "上传头像",
            description = "上传头像接口，返回头像访问地址"
    )
    public JsonData uploadAvatar(
            @Parameter(description = "上传文件", required = true)
            @RequestPart("file") MultipartFile file) {
        return accountService.uploadAvatar(file);
    }
}
