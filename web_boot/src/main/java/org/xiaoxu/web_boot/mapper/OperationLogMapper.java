package org.xiaoxu.web_boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xiaoxu.web_boot.entity.OperationLog;

public interface OperationLogMapper extends BaseMapper<OperationLog> {



    int insert(OperationLog operationLog);
}
