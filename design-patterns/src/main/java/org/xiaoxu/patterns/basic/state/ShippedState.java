package org.xiaoxu.patterns.basic.state;

/** 具体状态：已发货（在途中） */
public class ShippedState implements OrderState {

    @Override
    public void confirm(Order order) {
        System.out.println("买家确认收货");
        order.setState(new CompletedState());
    }

    @Override
    public String name() {
        return "已发货";
    }
}
