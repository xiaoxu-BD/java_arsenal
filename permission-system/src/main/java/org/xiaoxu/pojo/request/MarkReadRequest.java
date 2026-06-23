package org.xiaoxu.pojo.request;

import lombok.Data;

import java.util.List;

/**
 * 公告标记已读请求
 */
@Data
public class MarkReadRequest {

    /**
     * 单个公告 ID（与 ids 二选一）
     */
    private Long id;

    /**
     * 批量公告 ID（与 id 二选一）
     */
    private List<Long> ids;
}
