package org.xiaoxu.patterns.basic.state;

/** 具体状态：已支付（等待商家发货） */
public class PaidState implements OrderState {

    @Override
    public void ship(Order order) {
        System.out.println("商家已发货，物流揽收");
        order.setState(new ShippedState());
    }

    @Override
    public String name() {
        return "已支付";
    }
}
