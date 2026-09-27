package org.xiaoxu.patterns.basic.state;

/**
 * 状态模式演示（初级版）
 *
 * <p>课堂演示点：
 * <ol>
 *   <li>正常流程：待支付 -> 已支付 -> 已发货 -> 已完成；</li>
 *   <li>非法操作：刚下单就"发货"，状态类直接拒绝——
 *       如果不用状态模式，这些校验会散落成一堆 if-else。</li>
 * </ol>
 */
public class StateDemo {

    public static void main(String[] args) {
        Order order = new Order("SO-20240601-001");

        System.out.println("========== 1. 先试试走捷径 ==========");
        order.ship();      // 待支付状态不允许发货

        System.out.println("\n========== 2. 正常流程 ==========");
        order.pay();       // 待支付 -> 已支付
        order.ship();      // 已支付 -> 已发货
        order.confirm();   // 已发货 -> 已完成

        System.out.println("\n========== 3. 已完成后再支付 ==========");
        order.pay();       // 终态拒绝
    }
}
