package org.xiaoxu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String responseResult;
    private String operator;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Long costTime;
    /**
     * 写入类型：0-同步写入，1-MQ异步写入
     */
    private Integer writeType;
    private LocalDateTime createTime;
}
