package org.xiaoxu.patterns.basic.decorator;

/** 具体装饰器：加糖，价格 +1 */
public class Sugar extends CondimentDecorator {

    public Sugar(Beverage beverage) {
        super(beverage);
    }

    @Override
    public String getDescription() {
        return beverage.getDescription() + " + 糖";
    }

    @Override
    public double cost() {
        return beverage.cost() + 1.0;
    }
}
