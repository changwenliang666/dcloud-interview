package net.xdclass.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.xdclass.controller.req.AccountLoginReq;
import net.xdclass.controller.req.SendCheckCodeReq;
import net.xdclass.service.AccountService;
import net.xdclass.util.CaptchaUtil;
import net.xdclass.util.JsonData;
import net.xdclass.vo.AccountLoginResultVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "账号", description = "登录、验证码与头像")
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final CaptchaUtil captchaUtil;
    @Resource
    private AccountService accountService;

    public AccountController(CaptchaUtil captchaUtil) {
        this.captchaUtil = captchaUtil;
    }

    @Operation(summary = "获取图形验证码", description = "按手机号或邮箱生成图形验证码，返回 JPEG 图片")
    @GetMapping(value = "/get-captcha", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getCaptcha(@Parameter(description = "手机号或邮箱", required = true) @RequestParam(value = "identifier") String identifier,
                             @Parameter(description = "验证码类型：login-登录，register-注册", required = true) @RequestParam(value = "type") String type) {
        return captchaUtil.generateCaptchaImage(identifier, type);
    }

    @Operation(summary = "发送验证码", description = "校验图形验证码后发送短信或邮箱验证码")
    @PostMapping("/send-check-code")
    public JsonData sendCheckCode(@Valid @RequestBody SendCheckCodeReq req) {
        return accountService.sendCheckCode(req);
    }

    @Operation(summary = "用户登录", description = "使用手机号或邮箱和验证码登录")
    @PostMapping("/login")
    public JsonData login(@Valid @RequestBody AccountLoginReq req) {
        AccountLoginResultVO loginResult = accountService.login(req);
        return JsonData.buildSuccess(loginResult);
    }

    @Operation(summary = "查询用户列表", description = "按状态查询用户")
    @GetMapping("/get-all-user")
    public JsonData getAllUser(@Parameter(description = "用户状态：ON，OFF", required = true) @RequestParam("status") String status) {
        return JsonData.buildSuccess(accountService.getAllUser(status));
    }

    @Operation(summary = "上传头像", description = "上传头像接口，返回头像访问地址")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public JsonData uploadAvatar(@Parameter(description = "上传文件", required = true) @RequestPart("file") MultipartFile file) {
        return accountService.uploadAvatar(file);
    }
}
