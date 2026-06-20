package org.xiaoxu.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.dto.FulfillmentOrderCreateDTO;
import org.xiaoxu.workflow.dto.FulfillmentOrderQueryDTO;
import org.xiaoxu.workflow.dto.FulfillmentOrderUpdateDTO;
import org.xiaoxu.workflow.entity.FulfillmentOrder;
import org.xiaoxu.workflow.mapper.FulfillmentOrderMapper;
import org.xiaoxu.workflow.mapstruct.FulfillmentOrderMapStruct;
import org.xiaoxu.workflow.service.FlowableService;
import org.xiaoxu.workflow.service.FulfillmentOrderService;
import org.xiaoxu.workflow.vo.FulfillmentOrderVO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 履约单服务实现
 */
@Service
@RequiredArgsConstructor
public class FulfillmentOrderServiceImpl extends ServiceImpl<FulfillmentOrderMapper, FulfillmentOrder>
        implements FulfillmentOrderService {

    private final FulfillmentOrderMapper fulfillmentOrderMapper;
    private final FulfillmentOrderMapStruct fulfillmentOrderMapStruct;
    private final FlowableService flowableService;

    @Override
    @Transactional
    public FulfillmentOrderVO create(FulfillmentOrderCreateDTO dto, String applicant) {
        FulfillmentOrder order = fulfillmentOrderMapStruct.toEntity(dto);
        order.setOrderNo(generateOrderNo());
        order.setStatus(ApprovalStatus.DRAFT.name());
        order.setApplicant(applicant);
        order.setCreator(applicant);

        fulfillmentOrderMapper.insert(order);
        return fulfillmentOrderMapStruct.toVO(order);
    }

    @Override
    @Transactional
    public FulfillmentOrderVO update(FulfillmentOrderUpdateDTO dto) {
        FulfillmentOrder existing = fulfillmentOrderMapper.selectById(dto.getId());
        if (existing == null) {
            throw new RuntimeException("履约单不存在");
        }
        if (!ApprovalStatus.DRAFT.name().equals(existing.getStatus())) {
            throw new RuntimeException("只有草稿状态的履约单才能编辑");
        }

        FulfillmentOrder order = fulfillmentOrderMapStruct.toEntity(dto);
        order.setOrderNo(existing.getOrderNo());
        order.setStatus(existing.getStatus());
        order.setApplicant(existing.getApplicant());
        fulfillmentOrderMapper.updateById(order);

        return fulfillmentOrderMapStruct.toVO(fulfillmentOrderMapper.selectById(dto.getId()));
    }

    @Override
    public IPage<FulfillmentOrderVO> pageQuery(FulfillmentOrderQueryDTO query) {
        LambdaQueryWrapper<FulfillmentOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getApplicant())) {
            wrapper.eq(FulfillmentOrder::getApplicant, query.getApplicant());
        }
        if (StringUtils.hasText(query.getOrderNo())) {
            wrapper.like(FulfillmentOrder::getOrderNo, query.getOrderNo());
        }
        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(FulfillmentOrder::getTitle, query.getTitle());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(FulfillmentOrder::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(FulfillmentOrder::getCreateTime);

        IPage<FulfillmentOrder> page = fulfillmentOrderMapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()), wrapper);

        List<FulfillmentOrderVO> voList = page.getRecords().stream()
                .map(fulfillmentOrderMapStruct::toVO)
                .collect(Collectors.toList());

        Page<FulfillmentOrderVO> voPage = new Page<>(query.getCurrent(), query.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public FulfillmentOrderVO getDetailById(Long id) {
        FulfillmentOrder order = fulfillmentOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("履约单不存在");
        }
        return fulfillmentOrderMapStruct.toVO(order);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        FulfillmentOrder order = fulfillmentOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("履约单不存在");
        }
        fulfillmentOrderMapper.deleteById(id);
    }

    @Override
    @Transactional
    public FulfillmentOrderVO changeStatus(Long id, String status, String remark) {
        FulfillmentOrder order = fulfillmentOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("履约单不存在");
        }

        ApprovalStatus newStatus;
        try {
            newStatus = ApprovalStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的状态: " + status);
        }

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus.name());
        if (StringUtils.hasText(remark)) {
            order.setRemark(remark);
        }
        fulfillmentOrderMapper.updateById(order);

        return fulfillmentOrderMapStruct.toVO(order);
    }

    private void validateStatusTransition(String currentStatus, ApprovalStatus newStatus) {
        ApprovalStatus current = ApprovalStatus.valueOf(currentStatus);
        switch (current) {
            case DRAFT:
                if (newStatus != ApprovalStatus.PROCESSING && newStatus != ApprovalStatus.CANCELLED) {
                    throw new RuntimeException("草稿状态只能提交审批或取消");
                }
                break;
            case PROCESSING:
                if (newStatus != ApprovalStatus.APPROVED
                        && newStatus != ApprovalStatus.REJECTED
                        && newStatus != ApprovalStatus.CANCELLED) {
                    throw new RuntimeException("审批中状态只能通过、驳回或撤回");
                }
                break;
            case APPROVED:
            case REJECTED:
            case CANCELLED:
                throw new RuntimeException("终态不可变更");
            default:
                throw new RuntimeException("未知状态");
        }
    }

    @Override
    @Transactional
    public FulfillmentOrderVO submitApproval(Long id, String applicant) {
        FulfillmentOrder order = fulfillmentOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("履约单不存在");
        }
        if (!ApprovalStatus.DRAFT.name().equals(order.getStatus())) {
            throw new RuntimeException("只有草稿状态的履约单才能提交审批");
        }

        // 启动 Flowable 流程，businessKey 用 orderNo
        String processInstId = flowableService.startProcess(
                "fulfillment-approval",
                order.getOrderNo(),
                applicant,
                null
        );

        // 更新履约单状态
        order.setStatus(ApprovalStatus.PROCESSING.name());
        order.setProcessInstId(processInstId);
        fulfillmentOrderMapper.updateById(order);

        return fulfillmentOrderMapStruct.toVO(order);
    }

    private String generateOrderNo() {
        return "FO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
    }
}
