package net.xdclass.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.xdclass.service.ProductService;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商品", description = "权益与套餐查询")
@RequestMapping("/api/v1/product")
@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "查询上架权益", description = "返回状态为上架的权益列表")
    @GetMapping("/get-benefit")
    public JsonData getBenefitList() {
        return JsonData.buildSuccess(productService.getBenefit());
    }

    @Operation(summary = "查询上架套餐", description = "返回状态为上架的套餐，并带上套餐包含的权益")
    @GetMapping("/get-package-benefit")
    public JsonData getPackageBenefit() {
        return JsonData.buildSuccess(productService.getPackageBenefit());
    }
}
