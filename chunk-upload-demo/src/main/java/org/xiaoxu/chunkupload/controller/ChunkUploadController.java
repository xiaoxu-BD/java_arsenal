package org.xiaoxu.chunkupload.controller;

import org.xiaoxu.chunkupload.dto.InitRequest;
import org.xiaoxu.chunkupload.dto.InitResponse;
import org.xiaoxu.chunkupload.dto.MergeResponse;
import org.xiaoxu.chunkupload.dto.StatusResponse;
import org.xiaoxu.chunkupload.service.ChunkUploadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
public class ChunkUploadController {

    private final ChunkUploadService service;

    public ChunkUploadController(ChunkUploadService service) {
        this.service = service;
    }

    /** 第一步：初始化，后端生成 uploadId 并下发分片策略 */
    @PostMapping("/init")
    public InitResponse init(@RequestBody InitRequest request) {
        return service.init(request.fileName(), request.fileSize());
    }

    /** 断点续传：查询已上传的分片列表 */
    @GetMapping("/{uploadId}/status")
    public StatusResponse status(@PathVariable String uploadId) {
        return service.status(uploadId);
    }

    /** 上传单个分片 */
    @PostMapping("/{uploadId}/chunk/{index}")
    public void uploadChunk(@PathVariable String uploadId,
                            @PathVariable int index,
                            @RequestParam("file") MultipartFile file) throws IOException {
        service.uploadChunk(uploadId, index, file);
    }

    /** 全部分片传齐后合并 */
    @PostMapping("/{uploadId}/merge")
    public MergeResponse merge(@PathVariable String uploadId) throws IOException {
        return service.merge(uploadId);
    }
}
