package org.xiaoxu.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.xiaoxu.enums.UserStatusEnum;
import org.xiaoxu.handler.UserTypeHandler;

import java.time.LocalDateTime;

/**
 * @className: Users
 * @author: xiaoxu
 * @date: 2025/12/3 20:02
 * @Version: 1.0
 * @description:
 */

@Data
public class Users {
    private Long id;
    private String nickName;
    private String idCard;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtUpdate;
    private Integer deleted;
    private Integer version;
    @TableField(typeHandler = UserTypeHandler.class)
    private UserStatusEnum status;

}
