package org.xiaoxu.designpattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 监听器3 — 短信通知
 */
@Slf4j
@Component
public class SmsNotifyListener {

    @Async
    @Order(3)
    @EventListener(OrderCreatedEvent.class)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("【监听器-短信】发送下单成功短信: userId={}, orderNo={}", event.getUserId(), event.getOrderNo());
        // 调用短信服务...
    }
}
