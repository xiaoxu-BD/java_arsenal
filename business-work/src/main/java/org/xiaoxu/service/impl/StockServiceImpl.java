package org.xiaoxu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaoxu.domain.ProductStock;
import org.xiaoxu.mapper.ProductStockMapper;
import org.xiaoxu.service.StockService;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private ProductStockMapper productStockMapper;

    @Override
    public Integer queryAndLockInventory(Long productId) {
        ProductStock stock = productStockMapper.queryAndLockInventory(productId);
        return stock.getRemain();
    }

    @Override
    public void realDeductAmount(Integer quantity, Long productId) {
        productStockMapper.realDeductAmount(quantity,productId);
    }
}
