package org.xiaoxu.designpattern.adapter;

/**
 * 适配器模式 — 内部统一接口（目标接口）
 *
 * 业务代码只依赖此接口，不关心底层对接的是哪个外部系统
 */
public interface InnerOrderService {

    /**
     * 创建订单
     */
    String createOrder(String productId, int quantity);

    /**
     * 查询订单状态
     */
    String queryStatus(String orderNo);
}
