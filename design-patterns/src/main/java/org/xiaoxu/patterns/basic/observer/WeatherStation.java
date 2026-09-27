package org.xiaoxu.patterns.basic.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 天气站（Subject / 被观察者）
 *
 * <p>维护一份观察者名单，数据一到就"广播"。观察者想收消息就注册，不想要就退出——
 * 天气站不关心观察者是谁、要干什么，双方只靠 {@code update} 这一个约定耦合。
 *
 * <p>日常同款：MQ 广播、Spring 的 ApplicationEvent、GUI 的各种 Listener。
 */
public class WeatherStation {

    private final List<Display> displays = new ArrayList<>();

    /** 注册：想收消息就报名 */
    public void register(Display display) {
        displays.add(display);
    }

    /** 注销：不想收了随时退出 */
    public void remove(Display display) {
        displays.remove(display);
    }

    /** 数据到达：通知所有观察者 */
    public void measurementsChanged(double temperature, double humidity) {
        System.out.println("\n天气站收到新数据: " + temperature + "℃ / 湿度 " + humidity + "%，广播给 " + displays.size() + " 个观察者");
        for (Display display : displays) {
            display.update(temperature, humidity);
        }
    }
}
