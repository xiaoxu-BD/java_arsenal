package org.xiaoxu.patterns.basic.adapter;

/**
 * 被适配者（Adaptee）：家用电 220V，老旧但稳定，不能改也不想改
 */
public class Voltage220 {

    public int output220v() {
        return 220;
    }
}
