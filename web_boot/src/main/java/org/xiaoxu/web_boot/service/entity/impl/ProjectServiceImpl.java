package org.xiaoxu.web_boot.service.entity.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xiaoxu.web_boot.entity.domain.Project;
import org.xiaoxu.web_boot.mapper.ProjectMapper;
import org.xiaoxu.web_boot.service.entity.ProjectService;

/**
 * @className: ProjectServiceImpl
 * @author: xiaoxu
 * @date: 2025/12/28 11:10
 * @Version: 1.0
 * @description:
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {
}
