package org.xiaoxu.patterns.basic.state;

/**
 * 订单状态接口（State）：每个状态自己决定"收到某个操作后怎么办"
 *
 * <p>状态模式 vs 责任链（易混点）：责任链强调"请求谁来处理"（沿链找人），
 * 状态模式强调"我处于什么状态，同一操作就有不同行为"（状态之间自己跳转）。
 */
public interface OrderState {

    /** 支付：每个状态对"支付"的回应不同 */
    default void pay(Order order) {
        System.out.println("当前状态[" + name() + "]不允许支付");
    }

    /** 发货 */
    default void ship(Order order) {
        System.out.println("当前状态[" + name() + "]不允许发货");
    }

    /** 确认收货 */
    default void confirm(Order order) {
        System.out.println("当前状态[" + name() + "]不允许确认收货");
    }

    /** 状态名，用于打印 */
    String name();
}
