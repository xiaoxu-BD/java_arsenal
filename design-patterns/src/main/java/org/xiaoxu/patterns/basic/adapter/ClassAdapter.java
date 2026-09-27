package org.xiaoxu.patterns.basic.adapter;

/**
 * 类适配器：通过"继承"拿到 220V 的能力，再实现 5V 接口完成转换
 *
 * <p>缺点：Java 单继承，这里把唯一的继承名额用掉了，且继承的是"实现"而非"能力"。
 */
public class ClassAdapter extends Voltage220 implements Voltage5 {

    @Override
    public int output5v() {
        int v = output220v();          // 调用父类（被适配者）的方法
        System.out.println("类适配器降压: " + v + "V -> 5V");
        return v / 44;
    }
}
