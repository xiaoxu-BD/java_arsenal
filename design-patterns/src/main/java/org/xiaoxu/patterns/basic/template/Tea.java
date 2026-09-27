package org.xiaoxu.patterns.basic.template;

/** 具体模板：茶 */
public class Tea extends DrinkTemplate {

    private final boolean wantLemon;

    public Tea(boolean wantLemon) {
        this.wantLemon = wantLemon;
    }

    @Override
    protected void brew() {
        System.out.println("2. 用 80 度热水浸泡茶叶");
    }

    @Override
    protected void addCondiments() {
        System.out.println("4. 加一片柠檬");
    }

    /** 覆盖钩子：顾客说了算 */
    @Override
    protected boolean wantCondiments() {
        return wantLemon;
    }
}
