package org.xiaoxu.service;

public interface StockService {
    Integer queryAndLockInventory(Long productId);

    void realDeductAmount(Integer quantity, Long productId);
}
