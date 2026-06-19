package org.xiaoxu.workflow.approval;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.entity.FulfillmentOrder;
import org.xiaoxu.workflow.mapper.FulfillmentOrderMapper;

/**
 * 履约单审批回调。流程定义 key 为 {@code fulfillment-approval}，
 * businessKey 为 orderNo。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillmentApprovalHandler implements ApprovalHandler {

    private final FulfillmentOrderMapper fulfillmentOrderMapper;

    @Override
    public String supportProcessDefinitionKey() {
        return "fulfillment-approval";
    }

    @Override
    public void onApproved(ApprovalContext context) {
        updateOrderStatus(context.getBusinessKey(), ApprovalStatus.APPROVED);
    }

    @Override
    public void onRejected(ApprovalContext context) {
        updateOrderStatus(context.getBusinessKey(), ApprovalStatus.REJECTED);
    }

    @Override
    public Long getBusinessId(String businessKey) {
        // businessKey 为 orderNo，需查库反查主键 id
        FulfillmentOrder order = fulfillmentOrderMapper.selectOne(
                new LambdaQueryWrapper<FulfillmentOrder>()
                        .eq(FulfillmentOrder::getOrderNo, businessKey));
        return order == null ? null : order.getId();
    }

    private void updateOrderStatus(String businessKey, ApprovalStatus status) {
        FulfillmentOrder order = fulfillmentOrderMapper.selectOne(
                new LambdaQueryWrapper<FulfillmentOrder>()
                        .eq(FulfillmentOrder::getOrderNo, businessKey));
        if (order == null) {
            log.warn("履约单审批回调未找到订单, orderNo={}, 跳过状态更新", businessKey);
            return;
        }
        order.setStatus(status.name());
        fulfillmentOrderMapper.updateById(order);
    }
}
