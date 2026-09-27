package org.xiaoxu.patterns.advanced.singleton;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 枚举单例 —— 《Effective Java》钦定的最佳单例写法
 *
 * <p>三重免疫，写法还最短：
 * <ol>
 *   <li>免疫反射攻击：JDK 反射禁止对枚举 newInstance，攻击代码直接抛异常；</li>
 *   <li>免疫序列化破坏：反序列化返回同一个常量，不会造出第二个对象；</li>
 *   <li>线程安全：枚举常量的初始化由 JVM 保证只执行一次。</li>
 * </ol>
 *
 * <p>业务场景：全局单号生成器（所有线程取号必须走同一个计数器，否则单号会重复）。
 * 与 {@link ConfigHolder} 的取舍：需要懒加载/继承其他类时用 Holder，
 * 只要求"绝对单例"时枚举优先。
 */
public enum SequenceGenerator {

    /** 唯一实例 */
    INSTANCE;

    private final AtomicLong counter = new AtomicLong(20260909_000L);

    /** 生成下一个单号：AtomicLong 保证多线程取号不重号 */
    public long next() {
        return counter.incrementAndGet();
    }
}
