package org.xiaoxu.patterns.basic.builder;

/**
 * 电脑类：演示建造者模式（初级版）
 *
 * <p>痛点（课堂演示点）：电脑有必填项（CPU）和一堆可选项（内存、硬盘、显卡…）。
 * <ul>
 *   <li>用重载构造器：要写 N 个构造器组合，调用方还容易"参数传串位"——
 *       {@code new Computer("i7", 16, 1, true, false)} 谁知道第 4 个 true 是什么意思？</li>
 *   <li>用 setter：对象在 build 完成前处于"半成品"状态，还可能被忘了设置某项。</li>
 *   <li>建造者：链式调用，哪个字段设了什么一目了然，最后 build() 一次性成型。</li>
 * </ul>
 *
 * <p>JDK 中的同款：{@code StringBuilder.append(...).toString()}。
 */
public class Computer {

    private final String cpu;      // 必填
    private final int memory;      // 可选，单位 GB
    private final int disk;        // 可选，单位 GB
    private final boolean gpu;     // 可选，是否独显

    /** 构造器私有：外界只能通过 Builder 造电脑，防止绕过校验 */
    private Computer(Builder builder) {
        this.cpu = builder.cpu;
        this.memory = builder.memory;
        this.disk = builder.disk;
        this.gpu = builder.gpu;
    }

    public String getCpu() { return cpu; }
    public int getMemory() { return memory; }
    public int getDisk() { return disk; }
    public boolean hasGpu() { return gpu; }

    @Override
    public String toString() {
        return "Computer{cpu='" + cpu + "', memory=" + memory + "GB, disk=" + disk + "GB, gpu=" + gpu + "}";
    }

    /** 建造者：与 Computer 字段一一对应，链式设置 */
    public static class Builder {

        private final String cpu;          // 必填：放在 Builder 构造器里，想不填都不行
        private int memory = 8;            // 可选：给出默认值
        private int disk = 256;
        private boolean gpu = false;

        public Builder(String cpu) {
            this.cpu = cpu;
        }

        public Builder memory(int memory) {
            this.memory = memory;
            return this;                   // 返回自身，支持链式调用
        }

        public Builder disk(int disk) {
            this.disk = disk;
            return this;
        }

        public Builder gpu(boolean gpu) {
            this.gpu = gpu;
            return this;
        }

        /** 最后一步：真正造出 Computer，此后字段不可再改（全 final） */
        public Computer build() {
            return new Computer(this);
        }
    }
}
