package org.xiaoxu.patterns.basic.template;

/**
 * 模板方法模式演示（初级版）
 *
 * <p>课堂演示点：流程（1-4 步）完全一样，只有差异步骤不同；
 * 钩子让"柠檬茶"可以不加料——模板的流程还能被子类"轻度影响"。
 */
public class TemplateDemo {

    public static void main(String[] args) {
        System.out.println("========== 制作咖啡 ==========");
        new Coffee().make();

        System.out.println("\n========== 制作柠檬茶 ==========");
        new Tea(true).make();

        System.out.println("\n========== 制作原味茶（钩子生效，不加料） ==========");
        new Tea(false).make();
    }
}
