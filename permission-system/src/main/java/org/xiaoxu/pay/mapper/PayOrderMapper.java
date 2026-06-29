package org.xiaoxu.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.pay.entity.PayOrder;

/**
 * 付款单 Mapper
 */
@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {
}
