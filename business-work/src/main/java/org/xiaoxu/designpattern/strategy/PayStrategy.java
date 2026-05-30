package org.xiaoxu.designpattern.strategy;

/**
 * 策略接口 — 支付渠道
 *
 * Spring Boot 核心用法：@Autowired Map<String, PayStrategy>
 * Spring 自动将所有实现类注入为 map，key=beanName, value=实现类实例
 * 运行时按渠道编码动态选取，无需 if-else
 */
public interface PayStrategy {

    /**
     * 渠道标识（与 beanName 对应）
     */
    String channelCode();

    /**
     * 统一支付入口
     */
    String pay(String orderId, String amount);
}
