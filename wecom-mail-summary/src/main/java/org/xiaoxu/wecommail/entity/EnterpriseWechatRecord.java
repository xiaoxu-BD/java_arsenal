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
 * 企业微信群消息表 t_e_enterprise_wechat_record
 *
 * <p>注意:本表不存发送状态。某条消息"是否已成功发出"通过明细表关联 SUCCESS 发送记录动态判定。
 */
@Getter
@Setter
@TableName("t_e_enterprise_wechat_record")
public class EnterpriseWechatRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 企业微信 msgid,MQ 消费幂等键(唯一索引) */
    private String msgId;

    /** 当前仅处理 send */
    private String action = "send";

    private String fromUser;

    private String roomId;

    /** 消息时间(MQ msgtime 毫秒时间戳转换) */
    private LocalDateTime msgTime;

    /** 当前仅 text,保留字段便于扩展 */
    private String msgType;

    private String msgContent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
