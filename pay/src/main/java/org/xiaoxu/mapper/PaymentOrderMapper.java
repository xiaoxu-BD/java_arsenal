package org.xiaoxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.domain.entity.PaymentOrder;

// mapper/PaymentOrderMapper.java
@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
}