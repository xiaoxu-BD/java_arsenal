package org.xiaoxu.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.pay.entity.PayRecord;

/**
 * 支付记录 Mapper
 */
@Mapper
public interface PayRecordMapper extends BaseMapper<PayRecord> {
}
