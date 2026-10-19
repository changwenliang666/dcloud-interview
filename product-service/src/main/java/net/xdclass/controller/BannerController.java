package net.xdclass.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.vo.BannerDTO;
import net.xdclass.service.BannerService;
import net.xdclass.util.JsonData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Banner 查询接口
 */
@RestController
@RequestMapping("/api/banner/v1")
@Slf4j
public class BannerController {

    @Resource
    private BannerService bannerService;

    /**
     * 根据主键查询 Banner
     */
    @GetMapping("/detail")
    public JsonData detail(@RequestParam("id") Long id) {
        log.info("查询Banner详情, id:{}", id);
        BannerDTO bannerDTO = bannerService.findById(id);
        return JsonData.buildSuccess(bannerDTO);
    }

    /**
     * 查询 Banner 列表
     */
    @GetMapping("/list")
    public JsonData list(@RequestParam(value = "location", required = false) String location,
                         @RequestParam(value = "status", required = false) String status) {
        log.info("查询Banner列表, location:{}, status:{}", location, status);
        List<BannerDTO> bannerList = bannerService.list(location, status);
        return JsonData.buildSuccess(bannerList);
    }
}
