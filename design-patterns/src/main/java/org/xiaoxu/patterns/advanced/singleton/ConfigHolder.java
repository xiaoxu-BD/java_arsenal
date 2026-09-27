package org.xiaoxu.patterns.advanced.singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * 静态内部类单例（Holder 方式）—— 日常开发首选的"懒汉式"写法
 *
 * <p>和 basic 包的 DCL 相比，优点是零同步代码块也能做到"懒加载 + 线程安全"：
 * <ol>
 *   <li>Holder 的实例在 {@code ConfigHolder.INSTANCE} 第一次被访问时才初始化（懒）；</li>
 *   <li>JVM 保证类初始化是加锁的（JLS 12.4.2），别的线程看不到半成品（安全）；</li>
 *   <li>没有 volatile、没有双重判空，代码最短、最难写错。</li>
 * </ol>
 *
 * <p>业务场景：全局配置中心——系统只有一份配置，所有线程读同一个实例。
 */
public final class ConfigHolder {

    private final Map<String, String> config = new HashMap<>();

    private ConfigHolder() {
        // 防反射破坏：正常首次构造时 Holder.INSTANCE 还是 null，放行；
        // 之后反射再调本构造器时 INSTANCE 已存在，直接拦截
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException("单例已存在，禁止反射创建第二个实例");
        }
        // 实际项目：从配置文件 / 配置中心加载。这里放两条样例数据
        config.put("app.name", "java-arsenal");
        config.put("env", "dev");
    }

    /** 静态内部类：被引用时才加载，加载即由 JVM 保证单例 */
    private static final class Holder {
        private static final ConfigHolder INSTANCE = new ConfigHolder();
    }

    public static ConfigHolder getInstance() {
        return Holder.INSTANCE;
    }

    public String get(String key) {
        return config.get(key);
    }

    public void put(String key, String value) {
        config.put(key, value);
    }
}
