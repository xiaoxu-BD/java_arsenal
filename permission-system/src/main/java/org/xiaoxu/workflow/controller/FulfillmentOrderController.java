package org.xiaoxu.workflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.common.constants.PermissionConstants;
import org.xiaoxu.common.utils.Result;
import org.xiaoxu.workflow.dto.FulfillmentOrderCreateDTO;
import org.xiaoxu.workflow.dto.FulfillmentOrderQueryDTO;
import org.xiaoxu.workflow.dto.FulfillmentOrderUpdateDTO;
import org.xiaoxu.workflow.dto.FulfillmentStatusChangeDTO;
import org.xiaoxu.workflow.service.FulfillmentOrderService;
import org.xiaoxu.workflow.vo.FulfillmentOrderVO;

/**
 * 履约单管理控制器
 */
@RestController
@RequestMapping("/api/fulfillment-orders")
@RequiredArgsConstructor
public class FulfillmentOrderController {

    private final FulfillmentOrderService fulfillmentOrderService;

    /**
     * 创建履约单
     */
    @PostMapping
    public Result<FulfillmentOrderVO> create(@Valid @RequestBody FulfillmentOrderCreateDTO dto,
                                              Authentication authentication) {
        return Result.success(fulfillmentOrderService.create(dto, authentication.getName()));
    }

    /**
     * 更新履约单（仅草稿状态可编辑）
     */
    @PutMapping
    public Result<FulfillmentOrderVO> update(@Valid @RequestBody FulfillmentOrderUpdateDTO dto) {
        return Result.success(fulfillmentOrderService.update(dto));
    }

    /**
     * 分页查询履约单（只能查自己的）
     */
    @GetMapping
    public Result<IPage<FulfillmentOrderVO>> list(FulfillmentOrderQueryDTO query, Authentication authentication) {
        query.setApplicant(authentication.getName());
        return Result.success(fulfillmentOrderService.pageQuery(query));
    }

    /**
     * 履约单详情
     */
    @GetMapping("/{id}")
    public Result<FulfillmentOrderVO> detail(@PathVariable Long id) {
        return Result.success(fulfillmentOrderService.getDetailById(id));
    }

    /**
     * 删除履约单（仅草稿/已撤回状态可删）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fulfillmentOrderService.deleteById(id);
        return Result.success();
    }

    /**
     * 变更履约单状态（管理端人工干预入口：可直接把审批中的单子改为通过/驳回，
     * 会绕过 Flowable 审批与付款单生成，仅限拥有工作流管理权限的用户）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('" + PermissionConstants.WORKFLOW_ADMIN_LIST + "')")
    public Result<FulfillmentOrderVO> changeStatus(@PathVariable Long id,
                                                     @Valid @RequestBody FulfillmentStatusChangeDTO dto) {
        return Result.success(fulfillmentOrderService.changeStatus(id, dto.getStatus(), dto.getRemark()));
    }
}
