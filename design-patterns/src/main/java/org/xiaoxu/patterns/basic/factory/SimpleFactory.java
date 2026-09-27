package org.xiaoxu.patterns.basic.factory;

/**
 * 简单工厂（不属于 23 种 GoF 模式，但最常用，先讲它）
 *
 * <p>一个静态方法根据参数决定造什么产品，把 new 的活集中到一处。
 *
 * <p>缺点（引出工厂方法）：每加一种产品都要改这里的分支，违反"开闭原则"。
 */
public class SimpleFactory {

    public static Shape create(String type) {
        return switch (type) {
            case "circle" -> new Circle();
            case "square" -> new Square();
            case "triangle" -> new Triangle();
            // 新增图形 = 改这里 + 加产品类，工厂被反复修改
            default -> throw new IllegalArgumentException("未知图形类型: " + type);
        };
    }
}
