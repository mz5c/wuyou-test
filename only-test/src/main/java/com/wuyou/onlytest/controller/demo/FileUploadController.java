package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.service.demo.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        String filename = fileUploadService.upload(file);
        return Result.success(filename);
    }
}
