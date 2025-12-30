package org.xiaoxu.web_boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.web_boot.entity.domain.EmployeeProject;
import org.xiaoxu.web_boot.entity.domain.dto.EmployeeProjectDTO;

import java.util.List;

@Mapper
public interface EmployeeProjectMapper extends BaseMapper<EmployeeProject> {
    List<EmployeeProjectDTO> listByEmpIds(List<Long> empIds);

    void batchInsert(Long empId, @Param("projectIds") List<Long> projectIds);
}
