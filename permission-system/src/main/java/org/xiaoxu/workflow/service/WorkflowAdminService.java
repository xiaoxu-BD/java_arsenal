package org.xiaoxu.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.xiaoxu.workflow.dto.WorkflowAdminQueryDTO;
import org.xiaoxu.workflow.vo.WorkflowItemVO;

/**
 * 全部任务（管理员视角）服务：跨业务类型聚合查询请假/履约。
 */
public interface WorkflowAdminService {

    /**
     * 分页查询所有工作流条目（请假 + 履约），支持按业务类型 / 状态 / 申请人筛选。
     */
    IPage<WorkflowItemVO> queryAll(WorkflowAdminQueryDTO query);
}
