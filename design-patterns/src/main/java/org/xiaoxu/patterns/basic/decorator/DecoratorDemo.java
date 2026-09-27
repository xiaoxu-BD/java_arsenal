package org.xiaoxu.patterns.basic.decorator;

/**
 * 装饰器模式演示（初级版）
 *
 * <p>课堂演示点：菜单上永远只有 5 个类（1 咖啡 + 4 种料），却能组合出几十种饮品；
 * 新增"椰果"只需加一个类，完全不改旧代码。
 */
public class DecoratorDemo {

    public static void main(String[] args) {
        Beverage order = new Espresso();                       // 一杯浓缩
        System.out.println(order.getDescription() + " = " + order.cost() + "元");

        order = new Milk(order);                               // 加奶：牛奶包住咖啡
        System.out.println(order.getDescription() + " = " + order.cost() + "元");

        order = new Sugar(order);                              // 再加糖：糖包住"加奶咖啡"
        System.out.println(order.getDescription() + " = " + order.cost() + "元");

        order = new Milk(order);                               // 双份奶也没问题
        System.out.println(order.getDescription() + " = " + order.cost() + "元");

        // 结账时调用的是最外层装饰器的 cost()，像剥洋葱一样层层向内累加
    }
}
