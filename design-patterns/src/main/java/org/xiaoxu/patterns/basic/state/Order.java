package org.xiaoxu.patterns.basic.state;

/**
 * 环境类（Context）：订单
 *
 * <p>订单自己不写"if 已支付才能发货"这种判断，而是把操作全权委托给当前状态对象：
 * {@code state.ship(this)}。状态流转规则全部收进各个状态类里。
 */
public class Order {

    private final String orderNo;
    private OrderState state;

    public Order(String orderNo) {
        this.orderNo = orderNo;
        // 初始状态：待支付。状态对象无字段，可以做成单例复用（见 advanced 状态机）
        this.state = new CreatedState();
    }

    /** 换状态：只有状态类内部会调用 */
    public void setState(OrderState state) {
        this.state = state;
        System.out.println("订单[" + orderNo + "] 状态 -> " + state.name());
    }

    public OrderState getState() { return state; }
    public String getOrderNo() { return orderNo; }

    /* 三个业务操作：全部委托给当前状态 */

    public void pay() {
        state.pay(this);
    }

    public void ship() {
        state.ship(this);
    }

    public void confirm() {
        state.confirm(this);
    }
}
