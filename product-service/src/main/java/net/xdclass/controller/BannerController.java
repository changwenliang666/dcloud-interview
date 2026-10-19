package net.xdclass.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.service.BannerService;
import net.xdclass.util.JsonData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Banner 查询接口
 */
@Tag(name = "Banner", description = "Banner查询")
@RestController
@RequestMapping("/api/banner/v1")
@Slf4j
public class BannerController {

    @Resource
    private BannerService bannerService;

    @Operation(summary = "查询Banner详情", description = "根据主键查询 Banner")
    @GetMapping("/detail")
    public JsonData detail(@Parameter(description = "Banner主键", required = true) @RequestParam("id") Long id) {
        log.info("查询Banner详情, id:{}", id);
        return JsonData.buildSuccess(bannerService.findById(id));
    }

    @Operation(summary = "查询Banner列表", description = "location、status 为空时不参与过滤")
    @GetMapping("/list")
    public JsonData list(@Parameter(description = "位置，格式页面-位置-模块") @RequestParam(value = "location", required = false) String location,
                         @Parameter(description = "状态，ON在线，OFF下线") @RequestParam(value = "status", required = false) String status) {
        log.info("查询Banner列表, location:{}, status:{}", location, status);
        return JsonData.buildSuccess(bannerService.list(location, status));
    }
}
