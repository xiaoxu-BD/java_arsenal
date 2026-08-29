package org.xiaoxu.chunkupload.controller;

import org.xiaoxu.chunkupload.dto.InitRequest;
import org.xiaoxu.chunkupload.dto.InitResponse;
import org.xiaoxu.chunkupload.dto.MergeResponse;
import org.xiaoxu.chunkupload.dto.StatusResponse;
import org.xiaoxu.chunkupload.service.OssChunkUploadService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * OSS 分片上传接口，与 /api/upload 一一对应，另加一个 DELETE 用于取消上传
 */
@RestController
@RequestMapping("/api/oss-upload")
public class OssChunkUploadController {

    private final OssChunkUploadService service;

    public OssChunkUploadController(OssChunkUploadService service) {
        this.service = service;
    }

    @PostMapping("/init")
    public InitResponse init(@RequestBody InitRequest request) {
        return service.init(request.fileName(), request.fileSize());
    }

    @GetMapping("/{uploadId}/status")
    public StatusResponse status(@PathVariable String uploadId) {
        return service.status(uploadId);
    }

    @PostMapping("/{uploadId}/chunk/{index}")
    public void uploadChunk(@PathVariable String uploadId,
                            @PathVariable int index,
                            @RequestParam("file") MultipartFile file) throws IOException {
        service.uploadChunk(uploadId, index, file);
    }

    @PostMapping("/{uploadId}/merge")
    public MergeResponse merge(@PathVariable String uploadId) {
        return service.merge(uploadId);
    }

    @DeleteMapping("/{uploadId}")
    public void abort(@PathVariable String uploadId) {
        service.abort(uploadId);
    }
}
