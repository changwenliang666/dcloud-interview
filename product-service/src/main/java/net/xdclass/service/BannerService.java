package net.xdclass.service;

import net.xdclass.dto.BannerDTO;

import java.util.List;

/**
 * Banner 查询服务
 */
public interface BannerService {

    /**
     * 根据主键查询 Banner
     *
     * @param id 主键
     * @return Banner 信息
     */
    BannerDTO findById(Long id);

    /**
     * 查询 Banner 列表。location、status 为空时不参与过滤
     *
     * @param location 位置，格式页面-位置-模块
     * @param status   状态，ON 在线，OFF 下线
     * @return Banner 列表
     */
    List<BannerDTO> list(String location, String status);
}
