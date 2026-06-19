package org.xiaoxu.workflow.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.xiaoxu.workflow.dto.WorkflowAdminQueryDTO;
import org.xiaoxu.workflow.mapper.WorkflowAdminMapper;
import org.xiaoxu.workflow.service.WorkflowAdminService;
import org.xiaoxu.workflow.vo.WorkflowItemVO;

/**
 * 全部任务（管理员视角）服务实现：UNION ALL 跨表聚合 + 分页插件自动包外层 LIMIT。
 */
@Service
@RequiredArgsConstructor
public class WorkflowAdminServiceImpl implements WorkflowAdminService {

    private final WorkflowAdminMapper workflowAdminMapper;

    @Override
    public IPage<WorkflowItemVO> queryAll(WorkflowAdminQueryDTO query) {
        Page<WorkflowItemVO> page = new Page<>(query.getCurrent(), query.getSize());
        return workflowAdminMapper.selectAll(page, query);
    }
}
