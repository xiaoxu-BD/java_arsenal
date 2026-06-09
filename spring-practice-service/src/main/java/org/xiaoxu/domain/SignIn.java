package org.xiaoxu.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_sign_in")
public class SignIn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Date signDate;
    private Date createTime;
    private Byte repaird;
}
