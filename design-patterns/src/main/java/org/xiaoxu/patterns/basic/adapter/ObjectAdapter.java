package org.xiaoxu.patterns.basic.adapter;

/**
 * 对象适配器：通过"组合"持有 220V 电源，实现 5V 接口完成转换（推荐写法）
 *
 * <p>比类适配器好在哪（课堂演示点）：
 * <ol>
 *   <li>组合优于继承——不占用继承名额，还能适配 220V 的各种"子类电源"；</li>
 *   <li>只依赖被适配者的能力（output220v），不依赖它的实现细节。</li>
 * </ol>
 *
 * <p>JDK 中的同款：{@code new InputStreamReader(new FileInputStream(...))}
 * —— 把字节流"适配"成字符流。
 */
public class ObjectAdapter implements Voltage5 {

    private final Voltage220 voltage220;

    public ObjectAdapter(Voltage220 voltage220) {
        this.voltage220 = voltage220;
    }

    @Override
    public int output5v() {
        int v = voltage220.output220v();   // 委托给被适配者
        System.out.println("对象适配器降压: " + v + "V -> 5V");
        return v / 44;
    }
}
