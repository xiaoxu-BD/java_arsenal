package org.xiaoxu.patterns.basic.decorator;

/**
 * 饮品抽象类（Component）：咖啡和"加料"的公共父类
 *
 * <p>装饰器模式的核心设计：装饰者（加料）和被装饰者（咖啡）实现<b>同一个抽象</b>，
 * 于是"咖啡加奶"仍然是一杯饮品，还可以继续加糖、再加份浓缩——层层包裹、无限套娃。
 *
 * <p>JDK 中的同款：{@code new BufferedReader(new InputStreamReader(new FileInputStream(...)))}
 * —— 每个 IO 流都是对底层流的"加料"。
 */
public abstract class Beverage {

    /** 描述：装饰后累加，如 "浓缩咖啡 + 牛奶 + 糖" */
    public abstract String getDescription();

    /** 价格：装饰后累加 */
    public abstract double cost();
}
