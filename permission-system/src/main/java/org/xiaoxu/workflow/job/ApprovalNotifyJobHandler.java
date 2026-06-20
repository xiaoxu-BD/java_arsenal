package org.xiaoxu.workflow.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.pojo.SystemUsers;
import org.xiaoxu.service.EmailService;
import org.xiaoxu.workflow.entity.ApproveLeave;
import org.xiaoxu.workflow.entity.FulfillmentOrder;
import org.xiaoxu.workflow.mapper.ApproveLeaveMapper;
import org.xiaoxu.workflow.mapper.FulfillmentOrderMapper;

import java.util.List;

/**
 * 审批通过邮件通知兜底任务（XXL-Job）。
 * <p>
 * 定时扫描已通过但未发送邮件的审批记录，补发通知邮件。
 * 建议在 XXL-Job 管理后台配置 cron 表达式，如：0 0/5 * * * ?（每 5 分钟执行一次）。
 */
@Slf4j
@Component
public class ApprovalNotifyJobHandler {

    @Resource
    private ApproveLeaveMapper approveLeaveMapper;

    @Resource
    private FulfillmentOrderMapper fulfillmentOrderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmailService emailService;

    /**
     * 兜底补发请假审批通过邮件
     */
    @XxlJob("sendLeaveApprovalNotify")
    public void sendLeaveApprovalNotify() {
        List<ApproveLeave> unnotifiedList = approveLeaveMapper.selectUnnotifiedApproved();
        if (unnotifiedList.isEmpty()) {
            log.info("没有待通知的请假单");
            return;
        }
        log.info("发现 {} 条待通知的请假单", unnotifiedList.size());

        for (ApproveLeave leave : unnotifiedList) {
            try {
                SystemUsers user = userMapper.selectById(leave.getUserId());
                if (user == null || user.getEmail() == null) {
                    log.warn("请假单通知跳过：未找到申请人邮箱, userId={}, leaveId={}", leave.getUserId(), leave.getId());
                    continue;
                }
                String title = "您的请假申请已审批通过";
                String detailHtml = String.format(
                        "<b>请假类型：</b>%s<br><b>请假天数：</b>%s 天<br><b>请假原因：</b>%s<br><b>申请人：</b>%s",
                        leave.getLeaveType(), leave.getLeaveDay(), leave.getLeaveReason(), leave.getUserName());
                String html = emailService.buildEmailHtml(title,
                        "尊敬的 " + leave.getUserName() + "，您的请假申请已审批通过，请知悉。", detailHtml);
                emailService.sendHtmlEmail(user.getEmail(), title, html);
                approveLeaveMapper.markNotified(leave.getId());
                log.info("请假单邮件通知补发成功, leaveId={}", leave.getId());
            } catch (Exception e) {
                log.error("请假单邮件通知补发失败, leaveId={}", leave.getId(), e);
            }
        }
    }

    /**
     * 兜底补发履约单审批通过邮件
     */
    @XxlJob("sendFulfillmentApprovalNotify")
    public void sendFulfillmentApprovalNotify() {
        List<FulfillmentOrder> unnotifiedList = fulfillmentOrderMapper.selectUnnotifiedApproved();
        if (unnotifiedList.isEmpty()) {
            log.info("没有待通知的履约单");
            return;
        }
        log.info("发现 {} 条待通知的履约单", unnotifiedList.size());

        for (FulfillmentOrder order : unnotifiedList) {
            try {
                SystemUsers user = findUserByUsername(order.getApplicant());
                if (user == null || user.getEmail() == null) {
                    log.warn("履约单通知跳过：未找到申请人邮箱, applicant={}, orderId={}", order.getApplicant(), order.getId());
                    continue;
                }
                String title = "您的履约单已审批通过";
                String detailHtml = String.format(
                        "<b>订单号：</b>%s<br><b>标题：</b>%s<br><b>金额：</b>¥%s<br><b>申请人：</b>%s",
                        order.getOrderNo(), order.getTitle(), order.getAmount(), order.getApplicant());
                String html = emailService.buildEmailHtml(title,
                        "尊敬的 " + order.getApplicant() + "，您的履约单已审批通过，请知悉。", detailHtml);
                emailService.sendHtmlEmail(user.getEmail(), title, html);
                fulfillmentOrderMapper.markNotified(order.getId());
                log.info("履约单邮件通知补发成功, orderId={}", order.getId());
            } catch (Exception e) {
                log.error("履约单邮件通知补发失败, orderId={}", order.getId(), e);
            }
        }
    }

    private SystemUsers findUserByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<SystemUsers>()
                        .eq(SystemUsers::getUsername, username)
                        .eq(SystemUsers::getDeleted, "0"));
    }
}
