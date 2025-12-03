package org.xiaoxu.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryOperationEntity {
    private String orderId;
    private String status;
    private LocalDateTime createdAt;
}
