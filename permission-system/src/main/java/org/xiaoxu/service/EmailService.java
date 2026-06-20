package org.xiaoxu.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * 邮件发送服务（支持 HTML）
 */
@Slf4j
@Service
public class EmailService {

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送 HTML 邮件
     *
     * @param to         收件人邮箱
     * @param subject    邮件主题
     * @param htmlContent HTML 正文内容
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("邮件发送成功, to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("邮件发送失败, to={}, subject={}", to, subject, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 构建审批通过通知 HTML 邮件模板
     *
     * @param applicant  申请人
     * @param title      邮件标题行
     * @param subtitle   副标题文案
     * @param detailHtml 业务详情 HTML 片段
     * @return 完整 HTML 内容
     */
    public String buildEmailHtml(String title, String subtitle, String detailHtml) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0; padding:0; background-color:#f4f5f7; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7; padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="560" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                          <!-- 顶部色条 -->
                          <tr>
                            <td style="height:4px; background:linear-gradient(90deg,#165dff,#4f46e5);"></td>
                          </tr>
                          <!-- 正文 -->
                          <tr>
                            <td style="padding:36px 40px;">
                              <h2 style="margin:0 0 8px; font-size:20px; color:#1d2129;">%s</h2>
                              <p style="margin:0 0 24px; font-size:14px; color:#86909c;">%s</p>
                              <!-- 详情卡片 -->
                              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f7f8fa; border-radius:8px; padding:16px 20px; margin-bottom:24px;">
                                <tr>
                                  <td style="font-size:14px; color:#4e5969; line-height:1.8;">
                                    %s
                                  </td>
                                </tr>
                              </table>
                              <p style="margin:0; font-size:13px; color:#c9cdd4;">此邮件由系统自动发送，请勿回复。</p>
                            </td>
                          </tr>
                        </table>
                        <!-- 底部 -->
                        <p style="margin:16px 0 0; font-size:12px; color:#c9cdd4;">BPMN 权限管理系统</p>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(title, subtitle, detailHtml);
    }
}
