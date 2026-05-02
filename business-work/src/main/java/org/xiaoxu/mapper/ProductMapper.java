package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.domain.Product;

@Mapper
public interface ProductMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);
}