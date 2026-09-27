package org.xiaoxu.patterns.advanced.observer;

/** 事件：订单已支付 */
public record OrderPaidEvent(String orderNo, String payChannel) {
}
