package org.xiaoxu.patterns.basic.observer;

/** 具体观察者：户外大屏，只关心温度 */
public class BillboardDisplay implements Display {

    @Override
    public void update(double temperature, double humidity) {
        System.out.println("[大屏] === 今日温度 " + temperature + "℃ ===");
    }
}
