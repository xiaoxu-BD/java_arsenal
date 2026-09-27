package org.xiaoxu.patterns.basic.adapter;

/**
 * 适配器模式演示（初级版）
 *
 * <p>手机只认 5V 接口，不关心电从哪来、怎么变压——"手机面向接口，适配器负责转换"。
 */
public class AdapterDemo {

    public static void main(String[] args) {
        System.out.println("========== 1. 类适配器（继承方式） ==========");
        Voltage5 c = new ClassAdapter();
        charge(c);

        System.out.println("\n========== 2. 对象适配器（组合方式，推荐） ==========");
        Voltage5 o = new ObjectAdapter(new Voltage220());
        charge(o);
    }

    /** 手机充电：只依赖 5V 接口，插什么适配器都行 */
    private static void charge(Voltage5 voltage5) {
        System.out.println("手机获得 " + voltage5.output5v() + "V，开始充电");
    }
}
