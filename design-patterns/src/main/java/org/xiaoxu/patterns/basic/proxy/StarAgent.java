package org.xiaoxu.patterns.basic.proxy;

/**
 * 经纪人（静态代理 Proxy）：和明星实现同一接口，能干的事转交本人，琐事自己扛
 *
 * <p>代理 vs 装饰器（课堂易混点）：结构一模一样，意图不同——
 * 代理是"控制访问"（挡掉琐事、加校验），装饰器是"增强功能"（加料加价）。
 *
 * <p>静态代理的缺点：一个代理只能服务一个接口；接口加方法，代理类也得跟着改
 * ——由此引出 advanced 包里的 JDK 动态代理。
 */
public class StarAgent implements Star {

    private final Star star;

    public StarAgent(Star star) {
        this.star = star;
    }

    @Override
    public void negotiate() {
        System.out.println("经纪人：先聊聊档期和报价（前置处理）");
        // 谈判这种琐事经纪人直接回绝，不打扰明星 —— 这就是"控制访问"
    }

    @Override
    public void sing() {
        System.out.println("经纪人：确认商演资质通过，放行");
        star.sing();                                     // 专业活转交明星本人
        System.out.println("经纪人：结账收尾（后置处理）");
    }
}
