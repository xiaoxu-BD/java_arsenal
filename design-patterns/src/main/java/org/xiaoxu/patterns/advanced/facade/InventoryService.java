package org.xiaoxu.patterns.advanced.facade;

/** 子系统：库存服务 */
public class InventoryService {

    public void deductStock(String sku, int count) {
        System.out.println("[库存] 扣减 SKU=" + sku + " x" + count);
    }
}
