package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.controller.req.ProductBenefitOrderReq;
import net.xdclass.controller.req.ProductOrderPageReq;
import net.xdclass.controller.req.ProductPackageOrderReq;
import net.xdclass.enums.*;
import net.xdclass.exception.BizException;
import net.xdclass.interceptor.AccountLoginInterceptor;
import net.xdclass.mapper.ProductOrderMapper;
import net.xdclass.model.ProductOrderDO;
import net.xdclass.model.ProductPackageDO;
import net.xdclass.service.ProductService;
import net.xdclass.util.CommonUtil;
import net.xdclass.util.JsonData;
import net.xdclass.util.SpringBeanUtil;
import net.xdclass.util.WechatPayUtil;
import net.xdclass.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ProductOrderServiceImpl implements net.xdclass.service.ProductOrderService {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private WechatPayUtil wechatPayUtil;

    // 获取账号id
    private Long getCurrentAccountId() {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        return accountDTO.getId();
    }

    /**
     * 根据支付方式路由到对应的支付逻辑
     */
    private JsonData handlePayment(ProductOrderDO orderDO, String payType) {
        if (PaymentEnum.WECHAT_PAY.name().equals(payType)) {
            //微信支付
            return doWechatNativePay(orderDO);
        } else if (PaymentEnum.ALI_PAY.name().equals(payType)) {
            return JsonData.buildError("支付宝支付暂时未实现");
        }
        return JsonData.buildError("不支持的支付方式");
    }

    /**
     * 发起Native支付,构建支付信息
     */
    private JsonData doWechatNativePay(ProductOrderDO orderDO) {
        long payAmountFen = orderDO.getPayAmount().multiply(BigDecimal.valueOf(100)).longValue();
        String codeUrl = wechatPayUtil.createNativePayOrder(orderDO.getOutTradeNo(), payAmountFen, orderDO.getTitle());
        if (StringUtils.isBlank(codeUrl)) {
            log.error("微信支付下单失败");
            return JsonData.buildError("微信支付下单失败");
        }
        PayInfoVO payInfoVO = PayInfoVO.builder()
                .outTradeNo(orderDO.getOutTradeNo())
                .payAmount(payAmountFen)
                .payType(PaymentEnum.WECHAT_PAY.name())
                .codeUrl(codeUrl).build();

        log.info("微信支付下单成功，返回参数:{}", payInfoVO);
        return JsonData.buildSuccess(payInfoVO);
    }

    @Override
    public JsonData createBenefitOrder(ProductBenefitOrderReq req) {
        //生成订单编号
        String outTradeNo = String.format("%s%s%s", OrderIdPrefixEnum.BFT.name(), CommonUtil.getCurrentTimestamp(), CommonUtil.generateStringNumRandom(6));
        try {
            // 获取权益信息
            BenefitVO benefitVO = productService.getBenefitById(req.getId());
            if (benefitVO == null || benefitVO.getStatus().equals(StatusEnum.OFF.name())) {
                return JsonData.buildCodeAndMsg(BizCodeEnum.BENEFIT_NOT_EXIST.getCode(), BizCodeEnum.BENEFIT_NOT_EXIST.getMessage());
            }

            BigDecimal payAmount = benefitVO.getUnitPrice().multiply(new BigDecimal(req.getPurchaseCount())).multiply((req.getDiscount()));
            ProductOrderDO productOrderDO = ProductOrderDO.builder()
                    .outTradeNo(outTradeNo)
                    .accountId(getCurrentAccountId())
                    .orderType(OrderTypeEnum.BENEFIT_ORDER.name())
                    .title(benefitVO.getName())
                    .benefitId(benefitVO.getId())
                    .purchaseCount(req.getPurchaseCount())
                    .unitPrice(benefitVO.getUnitPrice())
                    .discount(req.getDiscount())
                    .payAmount(payAmount)
                    .orderState(OrderStateEnum.NEW.name())
                    .payType(req.getPaymentType())
                    .build();
            int rows = productOrderMapper.insert(productOrderDO);
            if (rows < 1) {
                log.error("创建订单失败:数据库插入失败");
                return JsonData.buildCodeAndMsg(BizCodeEnum.ORDER_ADD_FAIL.getCode(), BizCodeEnum.ORDER_ADD_FAIL.getMessage());
            }

            return handlePayment(productOrderDO, req.getPaymentType());
        } catch (Exception e) {
            log.error("创建订单失败", e);
            ProductOrderDO productOrderDO = productOrderMapper.selectOne(new LambdaQueryWrapper<ProductOrderDO>().eq(ProductOrderDO::getOutTradeNo, outTradeNo));
            if (productOrderDO != null) {
                int rows = productOrderMapper.deleteById(productOrderDO.getId());
                if (rows < 1) {
                    log.error("创建订单数据回滚失败");
                }
                log.info("订单数据已从数据库删除");
            }

            return JsonData.buildCodeAndMsg(BizCodeEnum.ORDER_ADD_FAIL.getCode(), BizCodeEnum.ORDER_ADD_FAIL.getMessage());
        }
    }

    @Override
    public JsonData createPackageOrder(ProductPackageOrderReq req) {
        //生成订单编号
        String outTradeNo = String.format("%s%s%s", OrderIdPrefixEnum.PKG.name(), CommonUtil.getCurrentTimestamp(), CommonUtil.generateStringNumRandom(6));
        try {
            ProductPackageVO productPackageVO = productService.getPackageById(req.getId());
            if (productPackageVO == null) {
                return JsonData.buildCodeAndMsg(BizCodeEnum.PACKAGE_NOT_EXIST.getCode(), BizCodeEnum.PRODUCT_NOT_EXIST.getMessage());
            }

            BigDecimal payAmount = productPackageVO.getPrice().multiply(new BigDecimal(req.getPurchaseCount())).multiply((req.getDiscount()));
            ProductOrderDO productOrderDO = ProductOrderDO.builder()
                    .outTradeNo(outTradeNo)
                    .accountId(getCurrentAccountId())
                    .orderType(OrderTypeEnum.PACKAGE_ORDER.name())
                    .title(productPackageVO.getName())
                    .productPackageId(productPackageVO.getId())
                    .purchaseCount(req.getPurchaseCount())
                    .unitPrice(productPackageVO.getPrice())
                    .discount(req.getDiscount())
                    .payAmount(payAmount)
                    .orderState(OrderStateEnum.NEW.name())
                    .payType(req.getPaymentType())
                    .build();
            int rows = productOrderMapper.insert(productOrderDO);
            if (rows < 1) {
                log.error("创建订单失败:数据库插入失败");
                return JsonData.buildCodeAndMsg(BizCodeEnum.ORDER_ADD_FAIL.getCode(), BizCodeEnum.ORDER_ADD_FAIL.getMessage());
            }

            return handlePayment(productOrderDO, req.getPaymentType());
        } catch (Exception e) {
            log.error("创建订单失败", e);
            ProductOrderDO productOrderDO = productOrderMapper.selectOne(new LambdaQueryWrapper<ProductOrderDO>().eq(ProductOrderDO::getOutTradeNo, outTradeNo));
            if (productOrderDO != null) {
                int rows = productOrderMapper.deleteById(productOrderDO.getId());
                if (rows < 1) {
                    log.error("创建订单数据回滚失败");
                }
                log.info("订单数据已从数据库删除");
            }

            return JsonData.buildCodeAndMsg(BizCodeEnum.ORDER_ADD_FAIL.getCode(), BizCodeEnum.ORDER_ADD_FAIL.getMessage());
        }
    }

    @Override
    public ProductOrderVO getOrderStatusByOutTradeNo(String outTradeNo) {
        LambdaQueryWrapper<ProductOrderDO> queryWrapper = new LambdaQueryWrapper<ProductOrderDO>().eq(ProductOrderDO::getOutTradeNo,outTradeNo).eq(ProductOrderDO::getAccountId,getCurrentAccountId());
         ProductOrderDO productOrderDO = productOrderMapper.selectOne(queryWrapper);
      return productOrderDO != null ? SpringBeanUtil.copyProperties(productOrderDO,ProductOrderVO.class) : null;
    }

    @Override
    public Map<String, Object> getOrderListByPage(ProductOrderPageReq req) {
        Long accountId = getCurrentAccountId();
        LambdaQueryWrapper<ProductOrderDO> queryWrapper = new LambdaQueryWrapper<ProductOrderDO>()
                .eq(ProductOrderDO::getAccountId, accountId)
                .eq(StringUtils.isNotBlank(req.getOrderType()), ProductOrderDO::getOrderType, req.getOrderType())
                .eq(StringUtils.isNotBlank(req.getPayType()), ProductOrderDO::getPayType, req.getPayType())
                .eq(StringUtils.isNotBlank(req.getOrderState()), ProductOrderDO::getOrderState, req.getOrderState())
                .orderByDesc(ProductOrderDO::getGmtCreate);

        Page<ProductOrderDO> page = new Page<>(req.getPageIndex(), req.getPageNum());
        Page<ProductOrderDO> resultPage = productOrderMapper.selectPage(page, queryWrapper);
        List<ProductOrderVO> orderList = SpringBeanUtil.copyProperties(resultPage.getRecords(), ProductOrderVO.class);
        log.info("分页查询订单成功, accountId:{}, total:{}", accountId, resultPage.getTotal());
        return CommonUtil.convertToPageMap(resultPage, orderList);
    }
}
