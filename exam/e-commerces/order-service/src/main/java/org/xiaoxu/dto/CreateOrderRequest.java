package org.xiaoxu.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @className: CreateOrderRequest
 * @author: xiaoxu
 * @date: 2025/11/29 9:20
 * @Version: 1.0
 * @description:
 */
@Setter
@Getter
@ToString
public class CreateOrderRequest {

    private String userId;
    private List<Item> items;

    @Data
    public static class Item {
        private String productId;
        private Integer quantity;
    }
}
