package com.wuyou.onlytest.service.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    @Value("${upload.path:./upload}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadPath));
        } catch (IOException e) {
            log.error("create upload dir failed", e);
        }
    }

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.endsWith(".jpg") || filename.endsWith(".png")
                || filename.endsWith(".pdf") || filename.endsWith(".txt"))) {
            throw new IllegalArgumentException("unsupported file type");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("file too large, max 10MB");
        }
        try {
            String savedName = UUID.randomUUID() + "_" + filename;
            Path target = Paths.get(uploadPath, savedName);
            Files.copy(file.getInputStream(), target);
            log.info("file saved: {}", target);
            return savedName;
        } catch (IOException e) {
            throw new RuntimeException("file upload failed", e);
        }
    }
}
