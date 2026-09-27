package org.xiaoxu.patterns.advanced.adapter;

/**
 * 老系统的日志类（被适配者）
 *
 * <p>祖传代码：没有级别、没有 traceId、方法名也不规范，但稳定跑了八年，
 * 底层的落盘/滚动策略都有人维护——不能动，也不想动。
 */
public class LegacyLogger {

    /** 只会"输出一行" */
    public void printLine(String line) {
        System.out.println("LegacyLogger -> " + line);
    }
}
