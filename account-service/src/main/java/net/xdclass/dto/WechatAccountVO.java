package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(name = "WechatAccountVO", description = "微信用户信息")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WechatAccountVO {

    @Schema(description = "用户的唯一标识")
    private String openid;
    /**
     * 如果开发者有在多个公众号，或在公众号、移动应用之间统一用户账号的需求，需要前往微信开放平台（open.weixin.qq.com）绑定公众号后，才可利用UnionID机制来满足上述需求
     * 换句话说，同一用户，对同一个微信开放平台下的不同应用，unionid是相同的
     */
    @Schema(description = "只有在用户将公众号绑定到微信开放平台账号后，才会出现该字段")
    private String unionid;

    @Schema(description = "用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息")
    private Integer subscribe;

    @Schema(description = "用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间")
    private Long subscribeTime;

    @Schema(description = "公众号运营者对粉丝的备注，公众号运营者可在微信公众平台用户管理界面对粉丝添加备注")
    private String remark;

    @Schema(description = "用户所在的分组ID（兼容旧的用户分组接口）")
    private Integer groupid;

    @Schema(description = "用户被打上的标签ID列表")
    private String tagidList;
}