package org.xiaoxu.se.apispi;

/**
 * SPI 接口 —— 框架定义规范，第三方来实现
 */
public interface LoggerSpi {
    void log(String message);
    String type();
}
