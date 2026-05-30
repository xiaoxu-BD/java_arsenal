package org.xiaoxu.designpattern.factory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工厂类 — 自动收集所有 ReportGenerator 实现，按 type 路由
 *
 * 与策略模式的区别：
 * - 策略模式：关注行为选择（怎么支付）
 * - 工厂模式：关注对象创建（创建哪种报表生成器）
 *
 * Spring Boot 实现上类似：都是 List 注入 + Map 建索引
 */
@Component
@RequiredArgsConstructor
public class ReportFactory {

    private final List<ReportGenerator> generators;
    private final Map<String, ReportGenerator> generatorMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (ReportGenerator gen : generators) {
            generatorMap.put(gen.type(), gen);
        }
    }

    /**
     * 按类型获取报表生成器
     */
    public ReportGenerator getGenerator(String type) {
        ReportGenerator gen = generatorMap.get(type);
        if (gen == null) {
            throw new IllegalArgumentException("不支持的报表类型: " + type);
        }
        return gen;
    }

    /**
     * 便捷方法：直接生成报表
     */
    public byte[] generate(String type, String data) {
        return getGenerator(type).generate(data);
    }
}
