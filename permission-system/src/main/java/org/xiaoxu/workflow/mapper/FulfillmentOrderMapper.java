package org.xiaoxu.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.workflow.entity.FulfillmentOrder;

/**
 * 履约单 Mapper
 */
@Mapper
public interface FulfillmentOrderMapper extends BaseMapper<FulfillmentOrder> {
}
