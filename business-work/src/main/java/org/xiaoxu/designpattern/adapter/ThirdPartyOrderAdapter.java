package org.xiaoxu.designpattern.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 适配器 — 将 ThirdPartyOrderApi 适配为 InnerOrderService
 *
 * 职责：
 * 1. 将内部参数格式转换为第三方格式（int quantity → String qty）
 * 2. 将第三方返回值转换为内部格式（XML → String, int → String）
 * 3. 业务代码只依赖 InnerOrderService，完全不感知第三方细节
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThirdPartyOrderAdapter implements InnerOrderService {

    private final ThirdPartyOrderApi thirdPartyApi;

    @Override
    public String createOrder(String productId, int quantity) {
        // 参数适配：int → String，补充 token
        String xml = thirdPartyApi.create(productId, String.valueOf(quantity), "internal_token");
        // 返回值适配：XML → 提取 orderNo
        String orderNo = extractBetween(xml, "<orderNo>", "</orderNo>");
        log.info("【适配器】创建订单完成: orderNo={}", orderNo);
        return orderNo;
    }

    @Override
    public String queryStatus(String orderNo) {
        int code = thirdPartyApi.getStatus(orderNo);
        // 返回值适配：int → String 状态描述
        return switch (code) {
            case 1 -> "PAID";
            case 2 -> "SHIPPED";
            case 3 -> "COMPLETED";
            default -> "UNKNOWN";
        };
    }

    private String extractBetween(String xml, String startTag, String endTag) {
        int start = xml.indexOf(startTag) + startTag.length();
        int end = xml.indexOf(endTag);
        return xml.substring(start, end);
    }
}
