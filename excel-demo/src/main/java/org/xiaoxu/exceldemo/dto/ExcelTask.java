package org.xiaoxu.exceldemo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 异步导入/导出任务, demo 存内存, 生产要落库或 Redis */
@Data
public class ExcelTask {

    public static final String TYPE_IMPORT = "IMPORT";
    public static final String TYPE_EXPORT = "EXPORT";

    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";

    private String id;
    private String type;
    private String status;

    /** 导出开始前就知道总数; 导入读不到总数, 只有 processed 一直涨 */
    private long total;
    private long processed;
    private long successCount;
    private long failCount;

    /** 产物: 导入=错误回执.xlsx(全对则空), 导出=结果文件 */
    private String filePath;

    private String errorMsg;
    private LocalDateTime createTime = LocalDateTime.now();
}
