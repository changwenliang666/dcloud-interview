package net.xdclass.service;

import net.xdclass.controller.req.ProductBenefitOrderReq;
import net.xdclass.controller.req.ProductOrderPageReq;
import net.xdclass.controller.req.ProductPackageOrderReq;
import net.xdclass.util.JsonData;
import net.xdclass.vo.ProductOrderVO;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ProductOrderService {
    JsonData createBenefitOrder(ProductBenefitOrderReq req);

    JsonData createPackageOrder(ProductPackageOrderReq req);

    ProductOrderVO getOrderStatusByOutTradeNo(String outTradeNo);

    Map<String, Object> getOrderListByPage(ProductOrderPageReq req);

    ResponseEntity<String> handleWechatPayCallback(String timestamp, String nonce, String signature,
                                                   String signType, String serialNumber, String body);
}
