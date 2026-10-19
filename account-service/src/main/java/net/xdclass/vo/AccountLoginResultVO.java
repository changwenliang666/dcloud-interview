package net.xdclass.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AccountLoginResultVO", description = "登录结果")
public class AccountLoginResultVO {

    @Schema(description = "是否登录成功")
    private boolean success;

    @Schema(description = "登录令牌")
    private String token;

    @Schema(description = "登录账号信息")
    private AccountDTO accountDTO;

    @Schema(description = "失败原因")
    private String errorMessage;

    @Schema(description = "二维码状态")
    private String qrCodeStatus;

    @Schema(description = "二维码场景值")
    private String sceneId;
}
