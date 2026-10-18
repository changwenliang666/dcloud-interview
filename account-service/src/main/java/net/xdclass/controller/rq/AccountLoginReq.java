package net.xdclass.controller.rq;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountLoginReq {
    @NotBlank(message = "手机号或者邮箱不能为空")
    private String identifier;
    @NotBlank(message="验证码不能为空")
    private String checkCode;
    @NotBlank(message="业务类型不能为空")
    private String type;
}
