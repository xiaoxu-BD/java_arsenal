package org.xiaoxu.designpattern.template;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 秒杀订单处理 — 覆写钩子方法，增加额外步骤
 */
@Slf4j
@Component
public class SecKillOrderHandler extends AbstractOrderHandler {

    @Override
    protected void validate(String userId, String productId) {
        log.info("【秒杀订单】校验: 用户限购检查、活动时间检查: userId={}, productId={}", userId, productId);
    }

    @Override
    protected void lockStock(String productId) {
        log.info("【秒杀订单】Redis预扣库存+DB乐观锁: productId={}", productId);
    }

    @Override
    protected String createOrder(String orderId, String userId, String productId) {
        String orderNo = "SK_" + orderId;
        log.info("【秒杀订单】创建订单: orderNo={}", orderNo);
        return orderNo;
    }

    @Override
    protected void afterCreate(String orderNo) {
        // 秒杀额外步骤：发MQ异步通知
        log.info("【秒杀订单】发MQ异步通知: orderNo={}", orderNo);
    }
}
