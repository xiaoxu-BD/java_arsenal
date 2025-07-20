package org.xiaoxu.web_boot.config.datasource;
/**
 * @className: DataSourceConfig
 * @author: xiaoxu
 * @date: 2025 7.14 20.58
 * @Version: 1.0
 * @description: 动态数据源上下文
 * 用于存储当前线程使用的数据源
 */
public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程使用的数据源
     * @param key 数据源的键值，通常是数据源的名称或标识符
     */
    public static void setDataSourceKey(String key) {
        CONTEXT_HOLDER.set(key);
    }

    /**
     * 获取当前线程使用的数据源
     */
    // 获取当前线程使用的数据源
    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }

    /**
     *
     * 清除当前线程的数据源
     */
    public static void clearDataSourceKey() {
        CONTEXT_HOLDER.remove();
    }
}
