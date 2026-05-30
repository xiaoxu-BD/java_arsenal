package org.xiaoxu.designpattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 监听器2 — 积分累积
 */
@Slf4j
@Component
public class PointsAccumulateListener {

    @Async
    @Order(2)
    @EventListener(OrderCreatedEvent.class)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("【监听器-积分】累积积分: userId={}, orderNo={}", event.getUserId(), event.getOrderNo());
        // 调用积分服务...
    }
}
