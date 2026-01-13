package org.xiaoxu.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime createTime;
}
