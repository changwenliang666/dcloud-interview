package net.xdclass.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户表，支持微信登录
 * </p>
 *
 * @author WenliangChang
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("account")
@Schema(name = "AccountDO", description = "用户表，支持微信登录")
public class AccountDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "电话号码")
    @TableField("phone")
    private String phone;

    @Schema(description = "头像")
    @TableField("head_img")
    private String headImg;

    @Schema(description = "状态：ON，OFF")
    @TableField("status")
    private String status;

    @Schema(description = "角色：COMMON普通用户，ADMIN超级会员")
    @TableField("role")
    private String role;

    @Schema(description = "微信openid")
    @TableField("openid")
    private String openid;

    @Schema(description = "微信unionid")
    @TableField("unionid")
    private String unionid;

    @Schema(description = "创建时间")
    @TableField("gmt_create")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    @TableField("gmt_modified")
    private Date gmtModified;
}
