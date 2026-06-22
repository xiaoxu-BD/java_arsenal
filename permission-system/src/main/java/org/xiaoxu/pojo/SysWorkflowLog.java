package org.xiaoxu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_workflow_log")
public class SysWorkflowLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String processInstanceId;
    private String taskId;
    private String taskName;
    private String businessType;
    private String businessKey;
    private String action;
    private String comment;
    private String operator;
    /**
     * 写入类型：0-同步写入，1-MQ异步写入
     */
    private Integer writeType;
    private LocalDateTime createTime;
}
