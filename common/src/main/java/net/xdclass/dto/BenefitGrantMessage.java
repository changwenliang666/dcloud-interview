package net.xdclass.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitGrantMessage {

    private String outTradeNo;   // 订单编号（用于幂等判断）
    private Long accountId;       // 用户账号ID（权益发放给谁）
    private Long orderId;         // 订单ID（account_benefit表的 product_order_id）
    private String orderType;     // 订单类型：BENEFIT_ORDER / PACKAGE_ORDER
    private List<BenefitItem> benefitItems; // 权益列表

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenefitItem {
        private Long benefitId;      // 权益ID
        private String benefitCode;  // 权益编码
        private Integer count;       // 发放次数
        private Integer validDays;   // 单次有效天数（null 默认 365 天）
    }
}