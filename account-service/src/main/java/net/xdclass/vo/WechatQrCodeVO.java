package net.xdclass.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "WechatQrCodeVO", description = "微信登录二维码")
public class WechatQrCodeVO {

    @Schema(description = "二维码场景值")
    private String sceneId;

    @Schema(description = "二维码地址")
    private String qrCodeUrl;

    @Schema(description = "过期时间，单位秒")
    private Integer expireTime;

    @Schema(description = "二维码状态")
    private String status;

    @Schema(description = "创建时间，毫秒时间戳")
    private Long createTime;
}
