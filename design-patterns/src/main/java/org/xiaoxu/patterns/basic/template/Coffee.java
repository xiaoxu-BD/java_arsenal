package org.xiaoxu.patterns.basic.template;

/** 具体模板：咖啡 */
public class Coffee extends DrinkTemplate {

    @Override
    protected void brew() {
        System.out.println("2. 用 92 度热水冲咖啡粉");
    }

    @Override
    protected void addCondiments() {
        System.out.println("4. 加糖和牛奶");
    }
    // 不覆盖钩子，走默认值：加料
}
