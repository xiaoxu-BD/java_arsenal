package org.xiaoxu.workflow.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.workflow.dto.WorkflowAdminQueryDTO;
import org.xiaoxu.workflow.vo.WorkflowItemVO;

/**
 * 全部任务（管理员视角）跨业务类型聚合查询 Mapper。
 * 用 UNION ALL 把 approve_leave / fulfillment_order 抽象成统一行。
 */
@Mapper
public interface WorkflowAdminMapper {

    /**
     * 分页查询所有工作流条目（请假 + 履约）。
     * 分页由 MyBatis-Plus PaginationInnerInterceptor 自动包外层 LIMIT。
     *
     * @param page  分页对象（由调用方传入，插件会回填 total/records）
     * @param query 业务类型 / 状态 / 申请人 等筛选条件
     */
    IPage<WorkflowItemVO> selectAll(IPage<WorkflowItemVO> page,
                                     @Param("query") WorkflowAdminQueryDTO query);
}
