package org.xiaoxu.patterns.basic.facade;

/** 子系统 3：音响 */
public class SoundSystem {

    public void on() {
        System.out.println("音响开机，音量调到 30");
    }

    public void off() {
        System.out.println("音响关机");
    }
}
