package org.xiaoxu.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户部门关联表
 */
@Data
@TableName("sys_user_department")
public class UserDepartment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 部门ID
     */
    private Long departmentId;
    
    /**
     * 是否主部门：0-否，1-是
     */
    private Integer isPrimary;
    
    private LocalDateTime createTime;
}
