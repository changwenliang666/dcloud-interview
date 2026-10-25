package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SmsDTO", description = "短信发送参数")
public class SmsDTO {

    @Schema(description = "接收短信的手机号码")
    private String mobile;

    @Schema(description = "模板参数，格式如12345")
    private String code;
}
