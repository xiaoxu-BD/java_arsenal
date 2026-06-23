package org.xiaoxu.workflow.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.service.EmailService;
import org.xiaoxu.workflow.constant.ProcessDefinitionKey;
import org.xiaoxu.workflow.entity.ApproveLeave;
import org.xiaoxu.workflow.entity.FulfillmentOrder;
import org.xiaoxu.workflow.mapper.ApproveLeaveMapper;
import org.xiaoxu.workflow.mapper.FulfillmentOrderMapper;
import org.xiaoxu.spring.SpringContextHolder;

/**
 * 审批通过邮件通知监听器。
 * <p>
 * 挂载在 BPMN 的 approvedEnd 节点上（event=end），
 * 当流程走到"审批通过"结束事件时触发，发送邮件通知申请人。
 */
@Slf4j
public class ApprovalEmailListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
        try {
            doNotify(execution);
        } catch (Exception e) {
            // 邮件发送失败不阻塞流程结束，仅记录日志，后续由 XXL-Job 兜底补发
            log.error("审批通过邮件通知失败, processInstanceId={}", execution.getProcessInstanceId(), e);
        }
    }

    private void doNotify(DelegateExecution execution) {
        // processDefinitionId 格式为 "leave-request:1:12345"，取冒号前的部分
        String processDefinitionId = execution.getProcessDefinitionId();
        String processDefinitionKey = processDefinitionId.split(":")[0];
        String businessKey = execution.getProcessInstanceBusinessKey();

        EmailService emailService = SpringContextHolder.getBean(EmailService.class);
        UserMapper userMapper = SpringContextHolder.getBean(UserMapper.class);

        String email = null;
        String title = null;

        if (ProcessDefinitionKey.FULFILLMENT_APPROVAL.equals(processDefinitionKey)) {
            // 履约单：businessKey = orderNo
            FulfillmentOrderMapper mapper = SpringContextHolder.getBean(FulfillmentOrderMapper.class);
            FulfillmentOrder order = mapper.selectOne(
                    new LambdaQueryWrapper<FulfillmentOrder>()
                            .eq(FulfillmentOrder::getOrderNo, businessKey));
            if (order == null) {
                log.warn("履约单不存在, orderNo={}, 跳过邮件通知", businessKey);
                return;
            }
            // 查申请人邮箱
            SystemUsers user = findUserByUsername(userMapper, order.getApplicant());
            if (user == null || user.getEmail() == null) {
                log.warn("未找到申请人邮箱, applicant={}, 跳过邮件通知", order.getApplicant());
                return;
            }
            email = user.getEmail();
            title = "您的履约单已审批通过";
            String detailHtml = String.format(
                    "<b>订单号：</b>%s<br><b>标题：</b>%s<br><b>金额：</b>¥%s<br><b>申请人：</b>%s",
                    order.getOrderNo(), order.getTitle(), order.getAmount(), order.getApplicant());
            String html = emailService.buildEmailHtml(title,
                    "尊敬的 " + order.getApplicant() + "，您的履约单已审批通过，请知悉。", detailHtml);
            emailService.sendHtmlEmail(email, title, html);
            // 标记已通知
            order.setNotified("1");
            mapper.updateById(order);

        } else if (ProcessDefinitionKey.LEAVE_REQUEST.equals(processDefinitionKey)) {
            // 请假单：businessKey = "用户名:identifier"
            ApproveLeaveMapper mapper = SpringContextHolder.getBean(ApproveLeaveMapper.class);
            String identifier = parseIdentifier(businessKey);
            if (identifier == null) {
                log.warn("无法解析 businessKey: {}, 跳过邮件通知", businessKey);
                return;
            }
            ApproveLeave leave = mapper.selectOne(
                    new LambdaQueryWrapper<ApproveLeave>()
                            .eq(ApproveLeave::getIdentifier, identifier));
            if (leave == null) {
                log.warn("请假单不存在, identifier={}, 跳过邮件通知", identifier);
                return;
            }
            // 查申请人邮箱
            SystemUsers user = userMapper.selectById(leave.getUserId());
            if (user == null || user.getEmail() == null) {
                log.warn("未找到申请人邮箱, userId={}, 跳过邮件通知", leave.getUserId());
                return;
            }
            email = user.getEmail();
            title = "您的请假申请已审批通过";
            String detailHtml = String.format(
                    "<b>请假类型：</b>%s<br><b>请假天数：</b>%s 天<br><b>请假原因：</b>%s<br><b>申请人：</b>%s",
                    leave.getLeaveType(), leave.getLeaveDay(), leave.getLeaveReason(), leave.getUserName());
            String html = emailService.buildEmailHtml(title,
                    "尊敬的 " + leave.getUserName() + "，您的请假申请已审批通过，请知悉。", detailHtml);
            emailService.sendHtmlEmail(email, title, html);
            // 标记已通知
            leave.setNotified("1");
            mapper.updateById(leave);

        } else {
            log.info("未知的流程定义 key: {}, 跳过邮件通知", processDefinitionKey);
        }
    }

    private SystemUsers findUserByUsername(UserMapper userMapper, String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<SystemUsers>()
                        .eq(SystemUsers::getUsername, username)
                        .eq(SystemUsers::getDeleted, "0"));
    }

    private String parseIdentifier(String businessKey) {
        if (businessKey == null || businessKey.isBlank()) {
            return null;
        }
        int idx = businessKey.lastIndexOf(':');
        if (idx < 0 || idx == businessKey.length() - 1) {
            return null;
        }
        return businessKey.substring(idx + 1);
    }
}
