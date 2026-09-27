package org.xiaoxu.wecommail.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.xiaoxu.wecommail.common.SendStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 邮件发送记录表 t_e_wechat_mail_send_record
 *
 * <p>uk(declare_info_id, send_date):一个群一天最多一封邮件;重试不新插记录,原记录状态流转。
 */
@Getter
@Setter
@TableName("t_e_wechat_mail_send_record")
public class MailSendRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long declareInfoId;

    /** 群ID冗余快照,便于审计 */
    private String roomId;

    /** 业务日期(哪一天的汇总) */
    private LocalDate sendDate;

    /** 第几轮:1=正点日报,>1=迟到消息补充轮(uk 为 群+日+轮次,支持一天多轮) */
    private Integer roundNo = 1;

    private SendStatus sendStatus;

    /** 实际发送成功时间 */
    private LocalDateTime sendTime;

    private String errorMessage;

    private Integer retryCount = 0;

    /** 本封邮件包含消息数(冗余) */
    private Integer messageCount = 0;

    /** 收件人快照,分号分隔 */
    private String toAddresses = "";

    /** 抄送快照,分号分隔 */
    private String ccAddresses = "";

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
