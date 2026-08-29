package org.xiaoxu.chunkupload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.AbortMultipartUploadRequest;
import com.aliyun.oss.model.CompleteMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadRequest;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.ListPartsRequest;
import com.aliyun.oss.model.PartETag;
import com.aliyun.oss.model.PartSummary;
import com.aliyun.oss.model.UploadPartRequest;
import com.aliyun.oss.model.UploadPartResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.chunkupload.dto.InitResponse;
import org.xiaoxu.chunkupload.dto.MergeResponse;
import org.xiaoxu.chunkupload.dto.StatusResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 阿里云 OSS 分片上传，接口形态与本地磁盘版 {@link ChunkUploadService} 完全同构：
 * init -> chunk(可断点续传) -> merge，只是存储层从本地磁盘换成了 OSS 的
 * initiateMultipartUpload -> uploadPart -> completeMultipartUpload。
 * <p>
 * OSS 分片上传的三个关键规则：
 * 1. partNumber 从 1 开始（最大 10000），本项目对外仍用 0 基下标，服务端 +1 转换
 * 2. 除最后一片外，每片至少 100KB（demo 固定 5MB，天然满足）
 * 3. complete 时必须携带每片的 PartETag 且按 partNumber 升序排列
 */
@Service
public class OssChunkUploadService {

    private static final Logger log = LoggerFactory.getLogger(OssChunkUploadService.class);

    private static final int DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024;

    @Value("${oss.endpoint:}")
    private String endpoint;

    @Value("${oss.accessKeyId:}")
    private String accessKeyId;

    @Value("${oss.accessKeySecret:}")
    private String accessKeySecret;

    @Value("${oss.bucketName:}")
    private String bucketName;

    @Value("${oss.urlPrefix:}")
    private String urlPrefix;

    private OSS ossClient;

    /** OSS 的 uploadId -> 上传任务（任务元数据在内存，与本地磁盘版一致；生产建议入库） */
    private final ConcurrentHashMap<String, OssUploadTask> tasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (endpoint.isBlank() || accessKeyId.isBlank() || bucketName.isBlank()) {
            log.warn("OSS 未配置（oss.endpoint / accessKeyId / bucketName），勾选 OSS 上传时接口会直接报错");
            return;
        }
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        log.info("OSS 客户端初始化完成, bucket={}, endpoint={}", bucketName, endpoint);
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    public InitResponse init(String fileName, long fileSize) {
        requireClient();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName 不能为空");
        }
        if (fileSize <= 0) {
            throw new IllegalArgumentException("fileSize 必须大于 0");
        }
        int chunkSize = (int) Math.min(DEFAULT_CHUNK_SIZE, fileSize);
        int totalChunks = (int) ((fileSize + chunkSize - 1) / chunkSize);
        if (totalChunks > 10000) {
            throw new IllegalArgumentException("文件过大: " + totalChunks + " 片超过 OSS 上限 10000");
        }

        // objectName 规则照抄 permission-system 的 OssService：目录/日期/uuid.后缀
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
        String objectName = "chunk-upload-demo/"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                + "/" + UUID.randomUUID() + extension;

        long start = System.nanoTime();
        InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(
                new InitiateMultipartUploadRequest(bucketName, objectName));
        String uploadId = result.getUploadId();

        tasks.put(uploadId, new OssUploadTask(objectName, fileName, fileSize, chunkSize, totalChunks));
        log.info("初始化 OSS 上传任务: uploadId={}, objectName={}, fileSize={}, chunkSize={}, totalChunks={}, 耗时 {} ms",
                uploadId, objectName, fileSize, chunkSize, totalChunks, (System.nanoTime() - start) / 1_000_000);
        return new InitResponse(uploadId, chunkSize, totalChunks);
    }

    public StatusResponse status(String uploadId) {
        OssUploadTask task = requireTask(uploadId);
        if (task.finished) {
            return toStatus(uploadId, task, List.of());
        }
        // 直接问 OSS 要已传分片，服务端重启丢内存也能续（只要任务表还在，objectName 就能找回）
        long start = System.nanoTime();
        List<PartSummary> parts = ossClient.listParts(
                new ListPartsRequest(bucketName, task.objectName, uploadId)).getParts();
        List<Integer> indexes = new ArrayList<>();
        for (PartSummary part : parts) {
            int index = part.getPartNumber() - 1;
            indexes.add(index);
            // 回填 PartETag，merge 时要用
            task.partETags.put(index, new PartETag(part.getPartNumber(), part.getETag()));
        }
        log.info("查询 OSS 分片进度: uploadId={}, 已传 {}/{}, 耗时 {} ms",
                uploadId, indexes.size(), task.totalChunks, (System.nanoTime() - start) / 1_000_000);
        return toStatus(uploadId, task, indexes);
    }

    public void uploadChunk(String uploadId, int index, MultipartFile file) throws IOException {
        OssUploadTask task = requireTask(uploadId);
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

        UploadPartRequest request = new UploadPartRequest();
        request.setBucketName(bucketName);
        request.setKey(task.objectName);
        request.setUploadId(uploadId);
        request.setInputStream(file.getInputStream());
        request.setPartSize(file.getSize());
        request.setPartNumber(index + 1); // OSS 的 partNumber 从 1 开始

        long start = System.nanoTime();
        UploadPartResult result = ossClient.uploadPart(request);
        task.partETags.put(index, result.getPartETag());
        log.info("OSS 分片落地: uploadId={}, index={}, size={}, 耗时 {} ms, 进度 {}/{}",
                uploadId, index, file.getSize(), (System.nanoTime() - start) / 1_000_000,
                task.partETags.size(), task.totalChunks);
    }

    public MergeResponse merge(String uploadId) {
        OssUploadTask task = requireTask(uploadId);
        if (task.finished) {
            return new MergeResponse(task.fileName, task.fileSize, task.mergedUrl);
        }
        // 内存里缺 etag 就向 OSS 对账一次（比如服务重启过、或分片是别的端续传的）
        if (task.partETags.size() != task.totalChunks) {
            syncFromOss(uploadId, task);
        }
        if (task.partETags.size() != task.totalChunks) {
            throw new IllegalStateException("分片未传齐: " + task.partETags.size() + "/" + task.totalChunks);
        }

        // complete 要求 PartETag 严格按 partNumber 升序
        List<PartETag> etags = new ArrayList<>(task.partETags.values());
        etags.sort(Comparator.comparingInt(PartETag::getPartNumber));

        long start = System.nanoTime();
        ossClient.completeMultipartUpload(
                new CompleteMultipartUploadRequest(bucketName, task.objectName, uploadId, etags));
        task.finished = true;
        task.mergedUrl = urlPrefix + "/" + task.objectName;
        log.info("OSS 合并完成: uploadId={}, fileName={}, size={}, 耗时 {} ms, 访问地址 {}",
                uploadId, task.fileName, task.fileSize, (System.nanoTime() - start) / 1_000_000, task.mergedUrl);
        return new MergeResponse(task.fileName, task.fileSize, task.mergedUrl);
    }

    /** 取消上传：OSS 端未 complete 的分片会一直占存储计费，abort 才会真正回收 */
    public void abort(String uploadId) {
        OssUploadTask task = requireTask(uploadId);
        ossClient.abortMultipartUpload(
                new AbortMultipartUploadRequest(bucketName, task.objectName, uploadId));
        tasks.remove(uploadId);
        log.info("已取消 OSS 上传任务: uploadId={}, objectName={}", uploadId, task.objectName);
    }

    private void syncFromOss(String uploadId, OssUploadTask task) {
        for (PartSummary part : ossClient.listParts(
                new ListPartsRequest(bucketName, task.objectName, uploadId)).getParts()) {
            task.partETags.put(part.getPartNumber() - 1, new PartETag(part.getPartNumber(), part.getETag()));
        }
    }

    private StatusResponse toStatus(String uploadId, OssUploadTask task, List<Integer> indexes) {
        return new StatusResponse(uploadId, task.fileName, task.chunkSize, task.totalChunks,
                indexes, task.finished, task.mergedUrl);
    }

    private void requireClient() {
        if (ossClient == null) {
            throw new IllegalStateException("OSS 未配置，请设置环境变量 OSS_ENDPOINT / OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET / OSS_BUCKET / OSS_URL_PREFIX");
        }
    }

    private OssUploadTask requireTask(String uploadId) {
        OssUploadTask task = tasks.get(uploadId);
        if (task == null) {
            throw new IllegalArgumentException("上传任务不存在或已过期: " + uploadId);
        }
        return task;
    }

    private long expectedChunkSize(OssUploadTask task, int index) {
        long remain = task.fileSize - (long) task.chunkSize * index;
        return Math.min(task.chunkSize, remain);
    }

    private static final class OssUploadTask {
        final String objectName;
        final String fileName;
        final long fileSize;
        final int chunkSize;
        final int totalChunks;
        /** 分片下标(0基) -> OSS 返回的 PartETag，complete 时必须全部带上 */
        final Map<Integer, PartETag> partETags = new ConcurrentSkipListMap<>();
        volatile boolean finished;
        volatile String mergedUrl;

        OssUploadTask(String objectName, String fileName, long fileSize, int chunkSize, int totalChunks) {
            this.objectName = objectName;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.chunkSize = chunkSize;
            this.totalChunks = totalChunks;
        }
    }
}
