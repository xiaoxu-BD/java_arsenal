package org.xiaoxu.wecommail.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class Order {
    private Long orderId;
    private Long userId;
    private String productName;
    private BigDecimal amount;
    private LocalDateTime createTime;
}