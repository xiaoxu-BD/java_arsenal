package org.xiaoxu.web_boot.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: xiaoxu
 * @Description: 操作日志
 *
 */
@Data
public class OperationLog {
    private Long id;
    private String className;
    private String methodName;
    private String params;
    private String result;
    private Long costTimeMs;
    private String exceptionMsg;
    private LocalDateTime createTime;
}
