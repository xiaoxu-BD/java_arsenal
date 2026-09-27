package org.xiaoxu.patterns.basic.facade;

/** 子系统 1：投影仪 */
public class Projector {

    public void on() {
        System.out.println("投影仪开机，切换到 HDMI");
    }

    public void off() {
        System.out.println("投影仪关机");
    }
}
