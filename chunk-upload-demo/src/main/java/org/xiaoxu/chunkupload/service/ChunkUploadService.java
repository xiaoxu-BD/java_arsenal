package org.xiaoxu.chunkupload.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.chunkupload.dto.InitResponse;
import org.xiaoxu.chunkupload.dto.MergeResponse;
import org.xiaoxu.chunkupload.dto.StatusResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 分片上传核心逻辑：分片大小、总片数、分片完整性校验、合并与总大小校验全部在后端完成，
 * 前端只是按后端下发的参数执行切片与传输。
 * <p>
 * 上传任务状态存内存（demo 最小化实现），生产环境应换成 Redis / 数据库以便多实例共享。
 */
@Service
public class ChunkUploadService {

    private static final Logger log = LoggerFactory.getLogger(ChunkUploadService.class);

    private static final int DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024;

    private final Path storageRoot;

    /** uploadId -> 上传任务 */
    private final ConcurrentHashMap<String, UploadTask> tasks = new ConcurrentHashMap<>();

    public ChunkUploadService(@Value("${chunk-upload.storage-dir:${java.io.tmpdir}/chunk-upload-demo}") String storageDir) throws IOException {
        this.storageRoot = Path.of(storageDir);
        Files.createDirectories(storageRoot);
        log.info("分片上传存储目录初始化完成: {}", storageRoot.toAbsolutePath());
    }

    public InitResponse init(String fileName, long fileSize) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName 不能为空");
        }
        if (fileSize <= 0) {
            throw new IllegalArgumentException("fileSize 必须大于 0");
        }
        int chunkSize = (int) Math.min(DEFAULT_CHUNK_SIZE, fileSize);
        int totalChunks = (int) ((fileSize + chunkSize - 1) / chunkSize);
        String uploadId = UUID.randomUUID().toString().replace("-", "");

        UploadTask task = new UploadTask(fileName, fileSize, chunkSize, totalChunks);
        tasks.put(uploadId, task);
        try {
            Files.createDirectories(chunkDir(uploadId));
        } catch (IOException e) {
            throw new IllegalStateException("创建分片目录失败: " + e.getMessage(), e);
        }
        log.info("初始化上传任务: uploadId={}, fileName={}, fileSize={}, chunkSize={}, totalChunks={}",
                uploadId, fileName, fileSize, chunkSize, totalChunks);
        return new InitResponse(uploadId, chunkSize, totalChunks);
    }

    public StatusResponse status(String uploadId) {
        UploadTask task = requireTask(uploadId);
        return new StatusResponse(uploadId, task.fileName, task.chunkSize, task.totalChunks,
                List.copyOf(task.uploadedChunks), task.finished, task.mergedPath);
    }

    public void uploadChunk(String uploadId, int index, MultipartFile file) throws IOException {
        UploadTask task = requireTask(uploadId);
        if (task.finished) {
            throw new IllegalStateException("该上传任务已合并完成，不能继续传分片");
        }
        if (index < 0 || index >= task.totalChunks) {
            throw new IllegalArgumentException("分片序号越界: " + index + ", 总片数: " + task.totalChunks);
        }
        long expected = expectedChunkSize(task, index);
        if (file.getSize() != expected) {
            throw new IllegalArgumentException("分片大小不符: 期望 " + expected + " 字节, 实际 " + file.getSize() + " 字节");
        }
        // 先写临时文件再原子移动，避免并发重传同一分片时读到半截文件
        long start = System.nanoTime();
        Path tmp = chunkDir(uploadId).resolve(index + ".tmp");
        Path target = chunkDir(uploadId).resolve(String.valueOf(index));
        file.transferTo(tmp.toFile());
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
        task.uploadedChunks.add(index);
        log.info("分片落地: uploadId={}, index={}, size={}, 耗时 {} ms, 进度 {}/{}",
                uploadId, index, file.getSize(), (System.nanoTime() - start) / 1_000_000,
                task.uploadedChunks.size(), task.totalChunks);
    }

    public MergeResponse merge(String uploadId) throws IOException {
        UploadTask task = requireTask(uploadId);
        if (task.finished) {
            return new MergeResponse(task.fileName, task.fileSize, task.mergedPath);
        }
        Set<Integer> uploaded = task.uploadedChunks;
        if (uploaded.size() != task.totalChunks) {
            throw new IllegalStateException("分片未传齐: " + uploaded.size() + "/" + task.totalChunks);
        }

        Path merged = storageRoot.resolve(sanitize(uploadId + "_" + task.fileName));
        log.info("开始合并: uploadId={}, fileName={}, totalChunks={}", uploadId, task.fileName, task.totalChunks);
        long start = System.nanoTime();
        long written = 0;
        try (OutputStream out = Files.newOutputStream(merged)) {
            for (int i = 0; i < task.totalChunks; i++) {
                written += Files.copy(chunkDir(uploadId).resolve(String.valueOf(i)), out);
            }
        }
        if (written != task.fileSize) {
            Files.deleteIfExists(merged);
            throw new IllegalStateException("合并后大小校验失败: 期望 " + task.fileSize + " 字节, 实际 " + written + " 字节");
        }
        // 分片已并入最终文件，清理临时目录
        try (var stream = Files.list(chunkDir(uploadId))) {
            for (Path p : stream.toList()) {
                Files.deleteIfExists(p);
            }
            Files.deleteIfExists(chunkDir(uploadId));
        }
        task.finished = true;
        task.mergedPath = merged.toAbsolutePath().toString();
        log.info("合并完成: uploadId={}, fileName={}, size={}, 耗时 {} ms, 保存至 {}",
                uploadId, task.fileName, written, (System.nanoTime() - start) / 1_000_000, task.mergedPath);
        return new MergeResponse(task.fileName, written, task.mergedPath);
    }

    private UploadTask requireTask(String uploadId) {
        UploadTask task = tasks.get(uploadId);
        if (task == null) {
            throw new IllegalArgumentException("上传任务不存在或已过期: " + uploadId);
        }
        return task;
    }

    private long expectedChunkSize(UploadTask task, int index) {
        long remain = task.fileSize - (long) task.chunkSize * index;
        return Math.min(task.chunkSize, remain);
    }

    private Path chunkDir(String uploadId) {
        return storageRoot.resolve(uploadId);
    }

    /** 只保留文件名部分，防止路径穿越 */
    private String sanitize(String fileName) {
        String name = Path.of(fileName).getFileName().toString();
        return name.isBlank() ? "unnamed" : name;
    }

    private static final class UploadTask {
        final String fileName;
        final long fileSize;
        final int chunkSize;
        final int totalChunks;
        final Set<Integer> uploadedChunks = new ConcurrentSkipListSet<>();
        volatile boolean finished;
        volatile String mergedPath;

        UploadTask(String fileName, long fileSize, int chunkSize, int totalChunks) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.chunkSize = chunkSize;
            this.totalChunks = totalChunks;
        }
    }
}
