package org.xiaoxu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.xiaoxu.domain.Product;
import org.xiaoxu.domain.dto.ProductInitRequest;
import org.xiaoxu.domain.dto.ProductRes;
import org.xiaoxu.mapper.ProductMapper;
import org.xiaoxu.service.ProductService;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Integer initProduct(ProductInitRequest request) {
        Product convertProduct = BeanUtil.copyProperties(request, Product.class);
        int record = productMapper.insert(convertProduct);
        if (record < 0){
            throw new RuntimeException("INIT FAILED");
        }
        return 1;
    }

    @Override
    public ProductRes getInfomation(Long productId) {
        Product product = productMapper.selectByPrimaryKey(productId);
        if (!Objects.isNull(product)){
            return BeanUtil.copyProperties(product, ProductRes.class);
        }
        return null;
    }
}
