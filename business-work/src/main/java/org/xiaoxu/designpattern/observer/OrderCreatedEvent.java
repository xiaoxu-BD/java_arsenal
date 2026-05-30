package org.xiaoxu.designpattern.observer;

import org.springframework.context.ApplicationEvent;

/**
 * 观察者模式 — 自定义事件
 *
 * 继承 ApplicationEvent，携带业务数据
 * 发布后所有 @EventListener 监听器自动收到通知
 */
public class OrderCreatedEvent extends ApplicationEvent {

    private final String orderNo;
    private final String userId;
    private final String productId;

    public OrderCreatedEvent(Object source, String orderNo, String userId, String productId) {
        super(source);
        this.orderNo = orderNo;
        this.userId = userId;
        this.productId = productId;
    }

    public String getOrderNo() { return orderNo; }
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
}
