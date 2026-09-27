package org.xiaoxu.patterns.basic.decorator;

/** 具体组件：被装饰的原始对象——一杯浓缩咖啡 */
public class Espresso extends Beverage {

    @Override
    public String getDescription() {
        return "浓缩咖啡";
    }

    @Override
    public double cost() {
        return 15.0;
    }
}
