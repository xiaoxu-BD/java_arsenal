package org.xiaoxu.wecommail.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 邮件发送:走 spring.mail.* 配置的真实 SMTP。
 * 显式设置 From(优先 spring.mail.from,缺省用 SMTP 用户名),否则部分服务商(如 163)会拒收。
 * SMTP 未配置时抛出明确异常,由发送服务记为 FAIL 并保留原因。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailSenderFacade {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final MailProperties mailProperties;

    /**
     * @param messageIdKey 同一轮发送固定不变(如 "12-2" 表示记录#12第2轮),生成稳定 Message-ID:
     *                     同轮失败重试复用同一 Message-ID,收件端可据此识别重复(极小概率的 at-least-once 重复)
     */
    public void send(List<String> to, List<String> cc, String subject, String htmlBody, String messageIdKey) throws Exception {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            throw new IllegalStateException("SMTP 未配置(spring.mail.*),无法发送真实邮件");
        }
        MimeMessage mimeMessage = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        String from = mailProperties.getProperties().get("mail.from") != null
                && !String.valueOf(mailProperties.getProperties().get("mail.from")).isBlank()
                ? String.valueOf(mailProperties.getProperties().get("mail.from")) : mailProperties.getUsername();
        if (from != null && !from.isBlank()) {
            helper.setFrom(from);
        }
        if (messageIdKey != null && !messageIdKey.isBlank()) {
            mimeMessage.setHeader("Message-ID", "<wecommail-" + messageIdKey + "@wecom-mail.local>");
        }
        helper.setTo(to.toArray(new String[0]));
        if (!cc.isEmpty()) {
            helper.setCc(cc.toArray(new String[0]));
        }
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        sender.send(mimeMessage);
        log.info("邮件已通过 SMTP 发送: from={} subject={} to={}", from, subject, to);
    }
}
