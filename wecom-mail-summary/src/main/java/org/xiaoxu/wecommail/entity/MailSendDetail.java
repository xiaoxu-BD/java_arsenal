package org.xiaoxu.wecommail.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 邮件发送明细表 t_e_wechat_mail_send_detail:记录"这一封邮件具体包含哪些微信群消息"
 */
@Getter
@Setter
@TableName("t_e_wechat_mail_send_detail")
public class MailSendDetail {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联 t_e_wechat_mail_send_record.id */
    private Long sendRecordId;

    /** 关联 t_e_enterprise_wechat_record.id */
    private Long messageId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
