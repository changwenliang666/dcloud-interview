package net.xdclass.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WechatQrCodeVO {
    private String sceneId;

    private String qrCodeUrl;

    private Integer expireTime;

    private String status;

    private Long createTime;
}
