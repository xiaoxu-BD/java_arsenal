package org.xiaoxu.patterns.advanced.adapter;

/**
 * 日志适配器演示（高级版）
 *
 * <p>业务方法只依赖 {@link LogGateway} 接口：
 * 今天传老系统适配器，明天换成 ELK 网关实现，biz 方法一行不改。
 */
public class AdapterDemo {

    public static void main(String[] args) {
        LogGateway gateway = new LegacyLoggerAdapter(new LegacyLogger());
        biz(gateway);
    }

    /** 业务代码：只见接口，不见任何具体日志实现 */
    private static void biz(LogGateway gateway) {
        gateway.debug("t-1001", "进入下单流程");
        gateway.info("t-1001", "下单成功");
        gateway.error("t-1001", "调用物流接口超时");
    }
}
