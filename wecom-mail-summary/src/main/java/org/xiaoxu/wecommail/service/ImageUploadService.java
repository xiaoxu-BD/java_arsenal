package org.xiaoxu.wecommail.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.xiaoxu.wecommail.dto.ImageUploadResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageUploadService {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/bmp", ".bmp",
            "image/webp", ".webp"
    );

    private final Path imageDirectory;

    public ImageUploadService(@Value("${app.upload.value:uploads/images}") String imageDirectory) {
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
    }

    public ImageUploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "图片大小不能超过10MB");
        }

        String contentType = file.getContentType();
        String extension = contentType == null ? null : IMAGE_EXTENSIONS.get(contentType.toLowerCase());
        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅支持 JPEG、PNG、GIF、BMP 和 WEBP 图片");
        }

        String fileName = UUID.randomUUID() + extension;
        Path target = imageDirectory.resolve(fileName);
        try {
            Files.createDirectories(imageDirectory);
            file.transferTo(target);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "图片保存失败", e);
        }

        return new ImageUploadResult(fileName, file.getSize(), target.toString());
    }
}
