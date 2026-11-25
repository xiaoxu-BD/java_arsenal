package org.xiaoxu.wecommail.payment.dto;

/**
 * 创建订单请求。requestId 由客户端生成用于幂等，商品价格等由服务端决定。
 */
public record CreateOrderRequest(String requestId, String productCode) {
}
