package org.xiaoxu.patterns.basic.state;

/** 具体状态：待支付（下单成功后的初始状态） */
public class CreatedState implements OrderState {

    @Override
    public void pay(Order order) {
        System.out.println("支付成功，扣款完成");
        order.setState(new PaidState());   // 支付 -> 已支付
    }

    @Override
    public String name() {
        return "待支付";
    }
}
