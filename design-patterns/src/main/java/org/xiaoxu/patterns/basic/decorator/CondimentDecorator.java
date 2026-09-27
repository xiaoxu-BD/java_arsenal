package org.xiaoxu.patterns.basic.decorator;

/**
 * 装饰器抽象类（Decorator）：持有一个 Beverage，自己也是 Beverage
 *
 * <p>"自己是饮品，又包着一杯饮品"——这一层抽象是整个模式的灵魂，
 * 加 3 次料也不需要新建任何"咖啡加奶加糖加奶盖"类。
 */
public abstract class CondimentDecorator extends Beverage {

    /** 被装饰的饮品（可能是原始咖啡，也可能是已经加过料的） */
    protected final Beverage beverage;

    protected CondimentDecorator(Beverage beverage) {
        this.beverage = beverage;
    }
}
