package org.xiaoxu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String className;
    private String methodName;
    private String params;
    private String result;
    private String sceneDesc;
    private Long costTimeMs;
    private String exceptionMsg;
    private LocalDateTime createTime;
}
