package net.xdclass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付信息DTO
 * 创建订单成功后返回给前端的支付参数
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayInfoVO {

    /**
     * 商户订单号
     */
    private String outTradeNo;

    /**
     * 支付金额（分）
     */
    private Long payAmount;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 微信Native支付二维码URL
     */
    private String codeUrl;
}