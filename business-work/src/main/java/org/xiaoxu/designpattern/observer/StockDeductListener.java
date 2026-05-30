package org.xiaoxu.designpattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 监听器1 — 库存扣减确认
 *
 * @EventListener: Spring 自动扫描并注册
 * @Order(1): 多个监听器的执行顺序
 * @Async: 异步执行，不阻塞主流程
 */
@Slf4j
@Component
public class StockDeductListener {

    @Async
    @Order(1)
    @EventListener(OrderCreatedEvent.class)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("【监听器-库存】确认扣减: orderNo={}, productId={}", event.getOrderNo(), event.getProductId());
        // 写入库存扣减流水...
    }
}
