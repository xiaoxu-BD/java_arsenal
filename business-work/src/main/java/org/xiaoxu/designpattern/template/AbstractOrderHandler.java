package org.xiaoxu.designpattern.template;

import lombok.extern.slf4j.Slf4j;

/**
 * 模板方法 — 下单处理骨架
 *
 * 定义算法骨架，将差异步骤延迟到子类
 * Spring Boot 中：每个子类加 @Component，按业务类型注入
 */
@Slf4j
public abstract class AbstractOrderHandler {

    /**
     * 模板方法 — 定义下单流程骨架（final 防止子类篡改流程）
     */
    public final void handleOrder(String orderId, String userId, String productId) {
        log.info("=== 开始处理订单 {} ===", orderId);

        // ① 校验（子类实现）
        validate(userId, productId);
        // ② 锁库存（子类实现）
        lockStock(productId);
        // ③ 创建订单（子类实现）
        String orderNo = createOrder(orderId, userId, productId);
        // ④ 后置处理（子类可选覆写）
        afterCreate(orderNo);

        log.info("=== 订单 {} 处理完成 ===", orderId);
    }

    protected abstract void validate(String userId, String productId);

    protected abstract void lockStock(String productId);

    protected abstract String createOrder(String orderId, String userId, String productId);

    /**
     * 钩子方法 — 子类可选覆写，不强制
     */
    protected void afterCreate(String orderNo) {
        log.info("默认后置处理: orderNo={}", orderNo);
    }
}
