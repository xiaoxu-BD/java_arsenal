package org.xiaoxu.patterns.advanced.adapter;

/**
 * 新版统一日志网关（目标接口）
 *
 * <p>背景：公司推行统一日志规范（分级 + traceId），但老系统只有一个"一行式"日志类。
 * 老类不能改（没人敢动），新规范要立刻用——适配器模式正好接住这种"新旧共存"期。
 */
public interface LogGateway {

    void info(String traceId, String message);

    void error(String traceId, String message);

    /** 分级输出，新业务一律走这个入口 */
    default void debug(String traceId, String message) {
        System.out.println("[DEBUG][" + traceId + "] " + message);
    }
}
