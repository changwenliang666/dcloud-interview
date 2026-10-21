package net.xdclass.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "AccountLoginReq", description = "登录请求")
public class AccountLoginReq {

    @NotBlank(message = "手机号或者邮箱不能为空")
    @Schema(description = "手机号或邮箱")
    private String identifier;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "短信或邮箱验证码")
    private String checkCode;

    @NotBlank(message = "业务类型不能为空")
    @Schema(description = "业务类型：login-登录，register-注册")
    private String type;
}
