package org.xiaoxu.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_user")
public class Users {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String phone;
    private String avatar;
    private String inviteCode;
    private Long inviteBy;
    private Date createTime;
}
