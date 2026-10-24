package net.xdclass.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.controller.req.ProductBenefitOrderReq;
import net.xdclass.controller.req.ProductOrderPageReq;
import net.xdclass.controller.req.ProductPackageOrderReq;
import net.xdclass.service.ProductOrderService;
import net.xdclass.util.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product-order")
@Slf4j
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

    @PostMapping("/callback/wechat_pay")
    public ResponseEntity<String> wechatPayCallback(
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
            @RequestHeader(value = "Wechatpay-Signature-Type", required = false) String signType,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serialNumber,
            @RequestBody String body) {

        log.info("收到微信支付回调，timestamp：{}，serial：{}", timestamp, serialNumber);
        log.info("收到微信支付回调，body：{}", body);

        return productOrderService.handleWechatPayCallback(
                timestamp, nonce, signature, signType, serialNumber, body);
    }


}
