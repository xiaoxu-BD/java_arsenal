package org.xiaoxu.domain;

/**
 * @className: SendDTO
 * @author: xiaoxu
 * @date: 2026/4/19 9:33
 * @Version: 1.0
 * @description:
 */

public class SendDTO {
    private String email;
    private String orderId;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
