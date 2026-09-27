package org.xiaoxu.patterns.advanced.observer;

import java.math.BigDecimal;

/**
 * 事件总线演示（高级版观察者）
 *
 * <p>业务价值：下单成功后要发短信、加积分、通知风控……以后还会加"发券"。
 * 下单代码只管 post 一个事件，后续动作各自订阅——发布方与订阅方完全解耦，
 * 新增动作零侵入。
 */
public class EventBusDemo {

    public static void main(String[] args) {
        EventBus bus = new EventBus();

        // 各业务模块各自订阅自己关心的事件
        bus.register(OrderCreatedEvent.class, event ->
                System.out.println("[短信] 通知 " + event.userId() + "：下单成功 " + event.orderNo()));
        bus.register(OrderCreatedEvent.class, event -> {
            // 故意抛异常：验证一个订阅者失败不拖垮其他订阅者
            throw new IllegalStateException("积分服务超时");
        });
        bus.register(OrderCreatedEvent.class, event ->
                System.out.println("[风控] 记录大额订单: " + event.amount()));
        bus.register(OrderPaidEvent.class, event ->
                System.out.println("[对账] " + event.orderNo() + " 走 " + event.payChannel() + " 支付"));

        System.out.println("========== 发布 OrderCreatedEvent ==========");
        bus.post(new OrderCreatedEvent("SO-1001", "u-9527", new BigDecimal("12999.00")));

        System.out.println("\n========== 发布 OrderPaidEvent ==========");
        bus.post(new OrderPaidEvent("SO-1001", "ALIPAY"));
    }
}
