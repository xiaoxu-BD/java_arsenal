package org.xiaoxu.chunkupload.dto;

import java.util.List;

/**
 * 上传进度查询响应，后端把分片策略一并下发，前端据此跳过已完成的分片实现断点续传
 */
public record StatusResponse(String uploadId, String fileName, int chunkSize, int totalChunks,
                             List<Integer> uploadedChunks, boolean finished, String mergedPath) {
}
