package org.xiaoxu.patterns.advanced.state;

/**
 * 订单状态机演示（高级版）
 *
 * <p>先打印整张流转规则表（评审/画图两用），再走一遍正常流程和两次非法流转。
 */
public class StateMachineDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. 流转规则表（就是需求文档本身） ==========");
        for (OrderStatus status : OrderStatus.values()) {
            System.out.printf("  %-5s -> %s%n", status.getDesc(),
                    OrderStateMachine.allowedNext(status).stream()
                            .map(OrderStatus::getDesc).toList());
        }

        System.out.println("\n========== 2. 正常流程 ==========");
        Order order = new Order("SO-3001");
        order.pay();
        order.ship();
        order.confirm();

        System.out.println("\n========== 3. 非法流转：已完成的订单想再支付 ==========");
        try {
            order.pay();
        } catch (IllegalStateException e) {
            System.out.println("被状态机拦截: " + e.getMessage());
        }

        System.out.println("\n========== 4. 已支付订单取消（走退款分支） ==========");
        Order another = new Order("SO-3002");
        another.pay();
        another.cancel();
    }
}
