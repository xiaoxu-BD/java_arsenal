package org.xiaoxu.patterns.basic.observer;

/**
 * 观察者接口（Observer）：气象站一有新数据，观察者们各自更新展示
 */
public interface Display {

    /** 收到最新天气后的动作 */
    void update(double temperature, double humidity);
}
