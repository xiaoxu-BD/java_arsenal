package org.xiaoxu.patterns.basic.decorator;

/** 具体装饰器：加牛奶，价格 +4 */
public class Milk extends CondimentDecorator {

    public Milk(Beverage beverage) {
        super(beverage);
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + " + 牛奶";
    }

    @Override
    public double cost() {
        return beverage.cost() + 4.0;    // 原价 + 加料价，层层向外累加
    }
}
