package net.xdclass.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendCheckCodeDTO {
    @NotBlank(message = "手机号不能为空")
    private String identifier;
    @NotBlank(message = "业务类型不能为空")
    private String type;
    @NotBlank(message = "图形验证码不能为空")
    private String captcha;
}
