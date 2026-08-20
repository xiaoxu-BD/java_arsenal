package org.xiaoxu.chunkupload.dto;

/**
 * 合并完成响应
 */
public record MergeResponse(String fileName, long size, String path) {
}
