package org.xiaoxu.designpattern.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 普通订单处理 — 实现模板方法的差异步骤
 */
@Slf4j
@Component
public class NormalOrderHandler extends AbstractOrderHandler {

    @Override
    protected void validate(String userId, String productId) {
        log.info("【普通订单】校验用户和商品: userId={}, productId={}", userId, productId);
    }

    @Override
    protected void lockStock(String productId) {
        log.info("【普通订单】扣减库存: productId={}", productId);
    }

    @Override
    protected String createOrder(String orderId, String userId, String productId) {
        String orderNo = "NML_" + orderId;
        log.info("【普通订单】创建订单: orderNo={}", orderNo);
        return orderNo;
    }
}
