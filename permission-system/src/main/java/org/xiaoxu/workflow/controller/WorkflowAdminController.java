package org.xiaoxu.workflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.common.utils.ExcelExportUtil;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.file.WorkflowItemExcelVO;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.constant.BusinessType;
import org.xiaoxu.workflow.dto.WorkflowAdminQueryDTO;
import org.xiaoxu.workflow.service.WorkflowAdminService;
import org.xiaoxu.workflow.vo.WorkflowItemVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全部任务（管理员视角）控制器。
 * 跨业务类型聚合查询请假/履约单，仅持有 workflow:admin:list 权限的角色可见。
 * <p>
 * 注意：菜单不显示 ≠ 接口不可访问，因此这里必须用 @PreAuthorize 兜底权限，
 * 防止任何登录用户构造 URL 直接拿到所有人的请假/履约数据。
 */
@RestController
@RequestMapping("/api/workflow/admin")
@RequiredArgsConstructor
public class WorkflowAdminController {

    private final WorkflowAdminService workflowAdminService;

    /**
     * 分页查询所有工作流条目（请假 + 履约），支持筛选。
     */
    @PreAuthorize("hasAuthority('workflow:admin:list')")
    @GetMapping("/all")
    public Result<IPage<WorkflowItemVO>> all(WorkflowAdminQueryDTO query) {
        return Result.success(workflowAdminService.queryAll(query));
    }

    /**
     * 导出所有工作流条目为 Excel。
     */
    @PreAuthorize("hasAuthority('workflow:admin:list')")
    @GetMapping("/export")
    public void exportAll(WorkflowAdminQueryDTO query, HttpServletResponse response) throws Exception {
        // 用大页取全量数据
        query.setCurrent(1);
        query.setSize(Integer.MAX_VALUE);
        IPage<WorkflowItemVO> page = workflowAdminService.queryAll(query);
        List<WorkflowItemVO> records = page.getRecords();

        List<WorkflowItemExcelVO> voList = records.stream().map(r -> {
            WorkflowItemExcelVO vo = new WorkflowItemExcelVO();
            vo.setBusinessType(BusinessType.getLabelByKey(r.getBusinessType()));
            vo.setBusinessKey(r.getBusinessKey());
            vo.setTitle(r.getTitle());
            vo.setApplicant(r.getApplicant());
            vo.setStatus(r.getStatus());
            vo.setAmount(r.getAmount());
            vo.setLeaveDay(r.getLeaveDay());
            vo.setCreateTime(r.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        ExcelExportUtil.write(response, "全部任务列表", "全部任务", WorkflowItemExcelVO.class, voList);
    }
}
