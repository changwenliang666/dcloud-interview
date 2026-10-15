package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Banner 返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BannerDTO", description = "Banner信息")
public class BannerDTO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "格式页面-位置-模块")
    private String location;

    @Schema(description = "中文名称")
    private String name;

    @Schema(description = "图片列表，逗号分隔")
    private String imgUrl;

    @Schema(description = "PC端跳转链接，逗号分隔")
    private String pcLink;

    @Schema(description = "文本")
    private String text;

    @Schema(description = "状态 ON在线，OFF下线")
    private String status;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    private Date gmtModified;
}
