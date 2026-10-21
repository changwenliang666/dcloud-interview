package net.xdclass.controller;

import jakarta.validation.Valid;
import net.xdclass.controller.req.ProductBenefitOrderReq;
import net.xdclass.controller.req.ProductOrderPageReq;
import net.xdclass.controller.req.ProductPackageOrderReq;
import net.xdclass.service.ProductOrderService;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product-order")
public class ProductOrderController {
    @Autowired
    private ProductOrderService productOrderService;

    @PostMapping("/benefit/create")
    public JsonData createBenefitOrder(@Valid @RequestBody ProductBenefitOrderReq req) {
        return productOrderService.createBenefitOrder(req);
    }

    @PostMapping("/package/create")
    public JsonData createPackageOrder(@Valid @RequestBody ProductPackageOrderReq req) {
        return productOrderService.createPackageOrder(req);
    }

    @GetMapping("/status")
    public JsonData getOrderStatusByOutTradeNo(@RequestParam(value = "outTradeNo") String outTradeNo) {
        return JsonData.buildSuccess(productOrderService.getOrderStatusByOutTradeNo(outTradeNo));
    }

    @PostMapping("/page")
    public JsonData getOrderListByPage(@Valid @RequestBody ProductOrderPageReq req) {
        return JsonData.buildSuccess(productOrderService.getOrderListByPage(req));
    }

}
