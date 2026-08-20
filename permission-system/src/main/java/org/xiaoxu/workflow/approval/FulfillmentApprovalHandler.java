package org.xiaoxu.workflow.approval;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.pay.service.PayService;
import org.xiaoxu.workflow.constant.ApprovalStatus;
import org.xiaoxu.workflow.entity.FulfillmentOrder;
import org.xiaoxu.workflow.mapper.FulfillmentOrderMapper;
import org.xiaoxu.pojo.SystemUsers;

/**
 * 履约单审批回调。流程定义 key 为 {@code fulfillment-approval}，
 * businessKey 为 orderNo。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillmentApprovalHandler implements ApprovalHandler {

    private final FulfillmentOrderMapper fulfillmentOrderMapper;
    private final UserMapper userMapper;
    private final PayService payService;

    @Override
    public String supportProcessDefinitionKey() {
        return "fulfillment-approval";
    }

    @Override
    public void onApproved(ApprovalContext context) {
        String orderNo = context.getBusinessKey();
        
        // 1. 更新履约单状态为已通过
        updateOrderStatus(orderNo, ApprovalStatus.APPROVED);
        
        // 2. 生成付款单
        FulfillmentOrder order = fulfillmentOrderMapper.selectOne(
                new LambdaQueryWrapper<FulfillmentOrder>()
                        .eq(FulfillmentOrder::getOrderNo, orderNo));
        
        if (order != null) {
            // 查询用户信息 此处Creator就是 所属人id
            String creator = order.getCreator();
            SystemUsers user = userMapper.selectOne(
                    new LambdaQueryWrapper<SystemUsers>().eq(SystemUsers::getUsername, creator));

            if (user == null || user.getId() == null) {
                // 创建人不存在时仅告警，不能让 NPE 吞掉整个回调（状态已更新但付款单会缺失）
                log.error("履约单创建人不存在，无法生成付款单: orderNo={}, creator={}", orderNo, creator);
                return;
            }

            // 创建付款单
            payService.createPayOrder(
                    "fulfillment",
                    order.getId(),
                    context.getProcessInstanceId(),
                    user.getId(),
                    user.getUsername(),
                    "履约单付款：" + order.getTitle(),
                    order.getAmount()
            );

            log.info("履约单审批通过，已生成付款单: orderNo={}", orderNo);
        }
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
