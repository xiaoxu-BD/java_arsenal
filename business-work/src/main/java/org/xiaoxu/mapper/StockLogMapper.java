package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.domain.StockLog;

@Mapper
public interface StockLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(StockLog record);

    int insertSelective(StockLog record);

    StockLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StockLog record);

    int updateByPrimaryKey(StockLog record);
}