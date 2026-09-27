package org.xiaoxu.patterns.advanced.adapter;

/**
 * 新老日志系统的"转接头"（对象适配器）
 *
 * <p>把新规范（级别 + traceId）翻译成老类听得懂的一行文本。
 * 新业务只认识 {@link LogGateway}，完全不知道 LegacyLogger 的存在；
 * 将来老日志类下线，只需换一个适配器实现（或直接换成真网关），业务代码零改动。
 *
 * <p>日常同款：slf4j 是门面，各日志实现是"可插拔"的适配层，道理相同。
 */
public class LegacyLoggerAdapter implements LogGateway {

    private final LegacyLogger legacy;

    public LegacyLoggerAdapter(LegacyLogger legacy) {
        this.legacy = legacy;
    }

    @Override
    public void info(String traceId, String message) {
        legacy.printLine("[INFO][" + traceId + "] " + message);   // 翻译成老类的"一行式"
    }

    @Override
    public void error(String traceId, String message) {
        legacy.printLine("[ERROR][" + traceId + "] " + message);
    }

    @Override
    public void debug(String traceId, String message) {
        // 老类没有 debug 概念，降级成 INFO 输出，附上说明——适配时"能力不对齐"的典型处理
        legacy.printLine("[INFO][" + traceId + "] (debug降级) " + message);
    }
}
