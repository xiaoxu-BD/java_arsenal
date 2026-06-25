package org.xiaoxu.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门信息表
 */
@Data
@TableName("sys_department")
public class Department {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 部门编码
     */
    private String deptCode;
    
    /**
     * 部门名称
     */
    private String deptName;
    
    /**
     * 上级部门ID
     */
    private Long parentId;
    
    /**
     * 部门经理用户名
     */
    private String managerUsername;
    
    /**
     * 部门状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 备注
     */
    private String remark;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String deleted;
}
