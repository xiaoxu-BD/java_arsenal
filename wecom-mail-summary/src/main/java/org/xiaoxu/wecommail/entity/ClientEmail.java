package org.xiaoxu.wecommail.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.xiaoxu.wecommail.common.EmailType;

import java.time.LocalDateTime;

/**
 * 邮箱配置表 t_e_client_email:一个群可配多个 TO / 多个 CC
 */
@Getter
@Setter
@TableName("t_e_client_email")
public class ClientEmail {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联 t_e_cargo_declare_info.id */
    private Long declareInfoId;

    private String emailAddress;

    private EmailType emailType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
