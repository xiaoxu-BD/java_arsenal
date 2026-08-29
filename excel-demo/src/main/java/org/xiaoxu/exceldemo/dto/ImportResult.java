package org.xiaoxu.exceldemo.dto;

import java.util.List;

/** 同步导入的结果: 合法行已入库, 非法行进 errors 清单 */
public record ImportResult(long total, long successCount, long failCount, List<UserImportErrorRow> errors) {
}
