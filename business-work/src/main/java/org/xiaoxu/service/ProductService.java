package org.xiaoxu.service;

import org.xiaoxu.domain.dto.ProductInitRequest;
import org.xiaoxu.domain.dto.ProductRes;

public interface ProductService {

    Integer initProduct(ProductInitRequest request);

    ProductRes getInfomation(Long productId);
}
