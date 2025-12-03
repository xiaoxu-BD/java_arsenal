package org.xiaoxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.pojo.OrderEntity;

/**
 * @className: OrderMapper
 * @author: xiaoxu
 * @date: 2025/11/29 9:19
 * @Version: 1.0
 * @description:
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
}
