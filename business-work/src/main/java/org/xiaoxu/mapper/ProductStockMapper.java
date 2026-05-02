package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.domain.ProductStock;

@Mapper
public interface ProductStockMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ProductStock record);

    int insertSelective(ProductStock record);

    ProductStock selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ProductStock record);

    int updateByPrimaryKey(ProductStock record);

    ProductStock queryAndLockInventory(Long productId);

    void realDeductAmount(@Param("quantity") Integer quantity, @Param("productId") Long productId);
}