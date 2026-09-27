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
 * 企业微信群列表缓存表 t_e_wechat_room:
 * 数据来自群列表接口的同步镜像(接入节点一:配置 roomId 时的下拉数据源)。
 * 仅存 roomId——群名称接口无法提供,由管理员配置时人工确认填写。
 * 消息不来自这里——消息由 MQ 推送(接入节点二),两者是不同的接入点。
 */
@Getter
@Setter
@TableName("t_e_wechat_room")
public class WechatRoom {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String roomId;

    /** 最近一次同步时间 */
    private LocalDateTime syncTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
