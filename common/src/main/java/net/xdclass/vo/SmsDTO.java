package net.xdclass.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsDTO {
    /**
     * 接收短信的手机号码
     */
    private String mobile;

    /**
     * 模板参数，格式如:12345
     */
    private String code;
}
