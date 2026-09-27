package org.xiaoxu.patterns.basic.adapter;

/**
 * 目标接口（Target）：手机想要的 5V 电
 *
 * <p>适配器模式解决的问题是：手里只有 220V（被适配者），接口却要 5V——
 * 接口不匹配，改哪边都不合适，中间加一个"转接头"。
 */
public interface Voltage5 {

    int output5v();
}
