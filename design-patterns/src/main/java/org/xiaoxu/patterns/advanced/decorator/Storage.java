package org.xiaoxu.patterns.advanced.decorator;

/**
 * 存储接口（高级版 Component）
 *
 * <p>业务背景：一个简单的 KV 存储，读写路径上经常要"加料"——
 * 打日志、失败重试、统计耗时。装饰器让这些能力像积木一样自由组合，
 * 而不是把 MemoryStorage 改成一个千百行的大杂烩。
 */
public interface Storage {

    void save(String key, String value);

    String read(String key);
}
