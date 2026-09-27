package org.xiaoxu.wecommail.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 微信群配置表 t_e_cargo_declare_info
 */
@Getter
@Setter
@TableName("t_e_cargo_declare_info")
public class CargoDeclareInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 企业微信群唯一标识,全局唯一(逻辑删除后重新添加走"复活"更新) */
    private String roomId;

    /** 群名称:接口无法提供,由管理员人工确认填写 */
    private String groupName;

    /** 每日固定发送时间 HH:mm */
    private LocalTime sendTime;

    /** 1 有效 0 逻辑删除 */
    private Integer valid = 1;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
