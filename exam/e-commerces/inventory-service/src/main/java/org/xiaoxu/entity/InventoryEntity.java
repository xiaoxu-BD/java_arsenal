package org.xiaoxu.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryEntity {
    private String productId;
    private Integer stock;
    private LocalDateTime updatedAt;
}