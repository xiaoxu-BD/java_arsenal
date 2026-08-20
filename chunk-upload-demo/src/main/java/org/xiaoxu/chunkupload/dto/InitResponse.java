package org.xiaoxu.chunkupload.dto;

/**
 * 初始化上传响应：后端算好分片大小与总片数，前端照单执行
 */
public record InitResponse(String uploadId, int chunkSize, int totalChunks) {
}
