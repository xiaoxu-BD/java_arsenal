package org.xiaoxu.patterns.advanced.state;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * 订单状态机（高级版状态模式：规则表实现）
 *
 * <p>basic 版把流转规则分散在四个状态类里，状态行为复杂时合适；
 * 但日常订单这种"流转规则本身就是需求文档"的场景，更工程化的做法是
 * <b>把全部流转规则集中成一张表</b>：
 * <ol>
 *   <li>规则一目了然，评审时对着表逐行核对，新状态加一行；</li>
 *   <li>canTransfer 是纯函数，单测好写、还能导出成状态流转图；</li>
 *   <li>非法流转统一在这里拦截，异常信息直接给出"当前允许去哪"，排查不用猜。</li>
 * </ol>
 *
 * <p>规则一旦上线基本不变，启动期建表、运行期只读，EnumMap 查找高效且线程安全。
 */
public final class OrderStateMachine {

    /** 流转规则表：当前状态 -> 允许到达的状态集合 */
    private static final Map<OrderStatus, Set<OrderStatus>> RULES = new EnumMap<>(OrderStatus.class);

    static {
        RULES.put(OrderStatus.CREATED, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED));
        RULES.put(OrderStatus.PAID, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        RULES.put(OrderStatus.SHIPPED, Set.of(OrderStatus.COMPLETED));
        RULES.put(OrderStatus.COMPLETED, Set.of());   // 终态
        RULES.put(OrderStatus.CANCELLED, Set.of());   // 终态
    }

    private OrderStateMachine() {
    }

    /** 判断流转是否合法（纯函数，方便单测与流程图导出） */
    public static boolean canTransfer(OrderStatus from, OrderStatus to) {
        return RULES.getOrDefault(from, Set.of()).contains(to);
    }

    /** 执行流转：非法流转直接抛异常，消息里给出合法去向 */
    public static void transfer(Order order, OrderStatus target) {
        OrderStatus current = order.getStatus();
        if (!canTransfer(current, target)) {
            throw new IllegalStateException(
                    "订单[" + order.getOrderNo() + "] 非法流转: "
                            + current.getDesc() + " -> " + target.getDesc()
                            + "，当前允许: " + allowedDesc(current));
        }
        order.setStatus(target);
        System.out.println("订单[" + order.getOrderNo() + "] "
                + current.getDesc() + " -> " + target.getDesc());
    }

    /** 当前状态允许到达哪些状态（给人看的报错 & 给流程图用的数据） */
    public static Set<OrderStatus> allowedNext(OrderStatus current) {
        return RULES.getOrDefault(current, Set.of());
    }

    private static String allowedDesc(OrderStatus current) {
        StringBuilder sb = new StringBuilder("[");
        for (OrderStatus next : allowedNext(current)) {
            sb.append(next.getDesc()).append(" ");
        }
        return sb.append("]").toString().trim();
    }
}
