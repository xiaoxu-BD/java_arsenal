package org.xiaoxu.web_boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.web_boot.entity.domain.Project;

/**
 * @className: ProjectMapper
 * @author: xiaoxu
 * @date: 2025/12/28 11:09
 * @Version: 1.0
 * @description:
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
