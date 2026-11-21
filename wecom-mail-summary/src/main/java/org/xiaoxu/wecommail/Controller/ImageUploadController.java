package org.xiaoxu.wecommail.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.wecommail.dto.ImageUploadResult;
import org.xiaoxu.wecommail.service.ImageUploadService;

@RestController
@RequestMapping("/upload")
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    public ImageUploadController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostMapping("/image")
    public ImageUploadResult uploadImage(@RequestParam("file") MultipartFile file) {
        return imageUploadService.upload(file);
    }
}
