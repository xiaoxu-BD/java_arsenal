package org.xiaoxu.web_boot.entity.practice;

import lombok.Data;

@Data
public class OrderSummary {
    private String orderId;
    private double totalPrice;
    private double discountRate;
    private double finalPrice;
    private String message;
}