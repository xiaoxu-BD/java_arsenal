package org.xiaoxu.patterns.basic.observer;

/**
 * 观察者模式演示（初级版）
 *
 * <p>课堂演示点：
 * <ol>
 *   <li>新增一种"观察者"（网页、手表…）天气站代码零修改——一对多解耦；</li>
 *   <li>第 3 次广播时手机已注销，只有大屏收到——订阅关系是动态的。</li>
 * </ol>
 */
public class ObserverDemo {

    public static void main(String[] args) {
        WeatherStation station = new WeatherStation();

        PhoneDisplay phone = new PhoneDisplay();
        BillboardDisplay billboard = new BillboardDisplay();

        station.register(phone);
        station.register(billboard);

        station.measurementsChanged(28.5, 60);

        System.out.println("\n---- 手机注销订阅 ----");
        station.remove(phone);

        station.measurementsChanged(31.0, 45);
    }
}
