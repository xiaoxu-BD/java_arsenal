package org.xiaoxu.patterns.basic.builder;

/**
 * 建造者模式演示（初级版）
 *
 * <p>对比两种写法的可读性：位置参数 vs 具名链式调用。
 */
public class BuilderDemo {

    public static void main(String[] args) {
        // 反例（注释掉）：这样的代码三个月后没人敢动
        // new Computer("i7", 16, 512, true);  // true 是什么？显卡？蓝牙？超频？

        // 正例：每个值是什么意思清清楚楚
        Computer office = new Computer.Builder("i5")
                .memory(16)
                .disk(512)
                .build();
        System.out.println("办公机: " + office);

        Computer game = new Computer.Builder("i9")
                .memory(32)
                .disk(2048)
                .gpu(true)
                .build();
        System.out.println("游戏机: " + game);

        // 只填必填项，其余走默认值
        Computer minimal = new Computer.Builder("i3").build();
        System.out.println("学生机: " + minimal);
    }
}
