package org.xiaoxu.web_boot.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.web_boot.utils.dto.BucketPolicyConfigDto;
import org.xiaoxu.web_boot.utils.dto.RustFSUploadResult;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.concurrent.TimeUnit;

/**
 * @className: RustFSUploadUtils
 * @author: xiaoxu
 * @date: 2025/8/3 20:26
 * @Version: 1.0
 * @description:
 */
@Component
@Slf4j
public class RustFSUploadUtils {

    private static final String UPLOAD_KEY = "user:avatar:upload";
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private S3Client s3Client;

    @Value("${rustfs.bucketName}")
    private String BUCKET_NAME;

    @Value("${rustfs.endpoint}")
    private String ENDPOINT;

    public String upload(MultipartFile file) {
        // 判断Bucket是否存在
        if (!bucketExists(BUCKET_NAME)) {
            // 创建Bucket
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(BUCKET_NAME)
                    .build());
            log.info("Bucket created: {}", BUCKET_NAME);

            // 添加Bucket的访问策略
            String policy = JSONUtil.toJsonStr(createBucketPolicyConfigDto(BUCKET_NAME));
            log.info("Bucket policy: {}", policy);

            PutBucketPolicyRequest policyReq = PutBucketPolicyRequest.builder()
                    .bucket(BUCKET_NAME)
                    .policy(policy)
                    .build();
            s3Client.putBucketPolicy(policyReq);
        } else {
            log.info("Bucket already exists.");
        }

        // 上传文件
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(file.getOriginalFilename())
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            RustFSUploadResult uploadResult = new RustFSUploadResult();
            uploadResult.setName(file.getOriginalFilename());
            uploadResult.setUrl(ENDPOINT + "/" + BUCKET_NAME + "/" + file.getOriginalFilename());



            redisUtils.set(UPLOAD_KEY, uploadResult,1000, TimeUnit.SECONDS);
            return uploadResult.getUrl();
        } catch (Exception e) {
            log.error("Error uploading file to RustFS: {}", e.getMessage());
            return null;
        }
    }



        /**
         * 判断Bucket是否存在
         */
        private boolean bucketExists(String bucketName) {
            try {
                s3Client.headBucket(request -> request.bucket(bucketName));
                return true;
            } catch (NoSuchBucketException exception) {
                return false;
            }
        }

    /**
     * 创建存储桶的访问策略，设置为只读权限
     */
    private BucketPolicyConfigDto createBucketPolicyConfigDto(String bucketName) {
        BucketPolicyConfigDto.Statement statement = BucketPolicyConfigDto.Statement.builder()
                .Effect("Allow")
                .Principal(BucketPolicyConfigDto.Principal.builder().AWS(new String[]{"*"}).build())
                .Action(new String[]{"s3:GetObject"})
                .Resource(new String[]{"arn:aws:s3:::" + bucketName + "/*"})
                .build();

        return BucketPolicyConfigDto.builder()
                .Version("2012-10-17")
                .Statement(CollUtil.toList(statement))
                .build();
    }



    }


