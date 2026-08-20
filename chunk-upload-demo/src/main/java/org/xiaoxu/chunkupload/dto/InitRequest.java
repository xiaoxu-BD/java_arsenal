package org.xiaoxu.chunkupload.dto;

/**
 * 初始化上传请求，前端只需要报上文件名和大小，分片策略由后端决定
 */
public record InitRequest(String fileName, long fileSize) {
}
