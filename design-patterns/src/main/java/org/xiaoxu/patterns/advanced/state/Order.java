package org.xiaoxu.patterns.advanced.state;

/**
 * 订单实体：业务方法只是"触发一个流转意图"，合法性由状态机裁决。
 * 没有 if(status == XX && ...) 的散弹式校验——加状态、改规则都不动这个类。
 */
public class Order {

    private final String orderNo;
    private OrderStatus status = OrderStatus.CREATED;

    public Order(String orderNo) {
        this.orderNo = orderNo;
    }

    /** 触发支付：CREATED -> PAID */
    public void pay() {
        OrderStateMachine.transfer(this, OrderStatus.PAID);
    }

    /** 触发货：PAID -> SHIPPED */
    public void ship() {
        OrderStateMachine.transfer(this, OrderStatus.SHIPPED);
    }

    /** 触发确认收货：SHIPPED -> COMPLETED */
    public void confirm() {
        OrderStateMachine.transfer(this, OrderStatus.COMPLETED);
    }

    /** 触发取消：CREATED/PAID -> CANCELLED */
    public void cancel() {
        OrderStateMachine.transfer(this, OrderStatus.CANCELLED);
    }

    public String getOrderNo() { return orderNo; }
    public OrderStatus getStatus() { return status; }
    void setStatus(OrderStatus status) { this.status = status; }
}
