package org.xiaoxu.wecommail.payment.dto;

/**
 * 发起支付响应，payUrl 指向模拟支付宝收银台页面。
 */
public record PayUrlResponse(String payUrl) {
}
