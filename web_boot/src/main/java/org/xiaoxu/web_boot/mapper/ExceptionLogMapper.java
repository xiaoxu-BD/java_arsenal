package org.xiaoxu.web_boot.mapper;

import java.util.List;
import org.xiaoxu.web_boot.entity.ExceptionLog;

public interface ExceptionLogMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ExceptionLog row);
    List<ExceptionLog> selectAll();
    int updateByPrimaryKey(ExceptionLog row);
}