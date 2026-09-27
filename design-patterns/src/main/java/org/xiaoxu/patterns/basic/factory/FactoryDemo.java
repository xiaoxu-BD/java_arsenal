package org.xiaoxu.patterns.basic.factory;

/**
 * 工厂三兄弟演示：简单工厂 / 工厂方法 / 抽象工厂（初级版）
 *
 * <p>课堂建议：先跑第 1 段，讲"new 收口"；再跑第 2 段，讲"开闭原则"；
 * 最后跑第 3 段，讲"产品族"——抽象工厂一次造一族（一个品牌的外设全套）。
 */
public class FactoryDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. 简单工厂：一个静态方法包打天下 ==========");
        Shape circle = SimpleFactory.create("circle");
        Shape square = SimpleFactory.create("square");
        circle.draw();
        square.draw();

        System.out.println("\n========== 2. 工厂方法：一种产品一个工厂 ==========");
        ShapeFactory factory = new CircleFactory();
        factory.create().draw();
        factory = new SquareFactory();   // 换工厂 = 换产品，调用方式不变
        factory.create().draw();

        System.out.println("\n========== 3. 抽象工厂：一次造一族（品牌外设全套） ==========");
        PcFactory dell = new DellFactory();
        dell.createMouse().click();
        dell.createKeyboard().type();

        PcFactory hp = new HpFactory();
        hp.createMouse().click();
        hp.createKeyboard().type();
    }

    /* ---------------- 以下嵌套类是抽象工厂的角色，类多但每个都极小 ---------------- */

    /** 抽象工厂：定义一个产品族的造法（鼠标 + 键盘） */
    interface PcFactory {
        Mouse createMouse();
        Keyboard createKeyboard();
    }

    interface Mouse {
        void click();
    }

    interface Keyboard {
        void type();
    }

    /** 具体工厂：戴尔全家桶 */
    static class DellFactory implements PcFactory {
        @Override public Mouse createMouse() { return new Mouse() {
            @Override public void click() { System.out.println("戴尔鼠标点击"); }
        }; }
        @Override public Keyboard createKeyboard() { return new Keyboard() {
            @Override public void type() { System.out.println("戴尔键盘打字"); }
        }; }
    }

    /** 具体工厂：惠普全家桶 */
    static class HpFactory implements PcFactory {
        @Override public Mouse createMouse() { return new Mouse() {
            @Override public void click() { System.out.println("惠普鼠标点击"); }
        }; }
        @Override public Keyboard createKeyboard() { return new Keyboard() {
            @Override public void type() { System.out.println("惠普键盘打字"); }
        }; }
    }
}
