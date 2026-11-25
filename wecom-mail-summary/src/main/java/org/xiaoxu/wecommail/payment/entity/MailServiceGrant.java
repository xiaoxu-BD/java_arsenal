package org.xiaoxu.wecommail.payment.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权益发放流水，orderNo 唯一保证一笔订单只发一次，与支付状态同一事务落库。
 */
@Data
public class MailServiceGrant {

    private String orderNo;
    private Long userId;
    private Integer serviceDays;
    private LocalDateTime grantedAt;
}
