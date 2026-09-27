package org.xiaoxu.patterns.basic.observer;

/** 具体观察者：手机屏幕 */
public class PhoneDisplay implements Display {

    @Override
    public void update(double temperature, double humidity) {
        System.out.println("[手机] 当前 " + temperature + "℃，湿度 " + humidity + "%，出门记得看天气");
    }
}
