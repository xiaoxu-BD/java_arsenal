package org.xiaoxu.patterns.basic.template;

/**
 * 冲饮品抽象类（模板方法模式）
 *
 * <p>模板方法 {@link #make()} 用 final 锁死流程骨架：烧水 -> 冲泡 -> 倒杯 -> 加料。
 * 茶和咖啡的差异被压缩到两个抽象方法里，子类"填空"即可，流程永远不会被改乱。
 *
 * <p>{@link #wantCondiments()} 是"钩子方法"：给了默认实现，子类可选择性覆盖，
 * 用来影响模板流程的走向（要不要加料）——比纯抽象方法更灵活。
 *
 * <p>JDK 中的同款：{@code AbstractList#get(int)} 定规则，ArrayList 填实现。
 */
public abstract class DrinkTemplate {

    /** 模板方法：final 防止子类改流程 */
    public final void make() {
        boilWater();
        brew();                 // 差异点 1：茶叶泡 / 咖啡冲
        pourInCup();
        if (wantCondiments()) { // 钩子控制流程走向
            addCondiments();    // 差异点 2：加柠檬 / 加糖奶
        }
    }

    /** 通用步骤：父类直接实现 */
    private void boilWater() {
        System.out.println("1. 把水烧开");
    }

    private void pourInCup() {
        System.out.println("3. 倒进杯子");
    }

    /** 差异步骤：交给子类 */
    protected abstract void brew();

    protected abstract void addCondiments();

    /** 钩子方法：默认加料，子类可覆盖为 false */
    protected boolean wantCondiments() {
        return true;
    }
}
