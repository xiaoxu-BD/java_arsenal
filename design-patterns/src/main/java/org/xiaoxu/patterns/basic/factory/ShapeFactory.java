package org.xiaoxu.patterns.basic.factory;

/**
 * 工厂方法（GoF 23 种之一）：一"具体产品"对应一"具体工厂"
 *
 * <p>和简单工厂的区别（课堂演示点）：把"造什么"这个决定从 if/else 分支，
 * 转移到"选择哪个工厂类"。新增产品只需新增一对 Product + Factory，
 * 完全不改旧代码——满足开闭原则。
 *
 * <p>JDK 中的同款：{@code Calendar.getInstance()}、{@code Executors.newFixedThreadPool()}。
 */
public abstract class ShapeFactory {

    /** 工厂方法：子类决定造哪种产品 */
    protected abstract Shape create();
}
