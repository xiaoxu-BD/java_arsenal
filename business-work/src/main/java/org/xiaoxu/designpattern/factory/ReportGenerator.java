package org.xiaoxu.designpattern.factory;

/**
 * 工厂模式 — 产品接口
 */
public interface ReportGenerator {

    /**
     * 报表类型标识
     */
    String type();

    /**
     * 生成报表
     */
    byte[] generate(String data);
}
