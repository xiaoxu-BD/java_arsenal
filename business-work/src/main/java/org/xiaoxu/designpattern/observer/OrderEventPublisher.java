package org.xiaoxu.designpattern.observer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 事件发布者
 *
 * 注入 ApplicationEventPublisher，调用 publishEvent() 即可
 * 所有 @EventListener(OrderCreatedEvent.class) 的监听器会自动执行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishOrderCreated(String orderNo, String userId, String productId) {
        log.info("【事件发布】订单创建: orderNo={}", orderNo);
        eventPublisher.publishEvent(new OrderCreatedEvent(this, orderNo, userId, productId));
    }
}
