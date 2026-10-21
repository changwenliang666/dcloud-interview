package net.xdclass.controller.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SendCheckCodeReq", description = "发送验证码请求")
public class SendCheckCodeReq {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号或邮箱")
    private String identifier;

    @NotBlank(message = "业务类型不能为空")
    @Schema(description = "业务类型：login-登录，register-注册")
    private String type;

    @NotBlank(message = "图形验证码不能为空")
    @Schema(description = "图形验证码")
    private String captcha;
}
