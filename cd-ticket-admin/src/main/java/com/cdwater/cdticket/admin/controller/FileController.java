package com.cdwater.cdticket.admin.controller;

import com.cdwater.cdticket.admin.security.AdminAuthorizer;
import com.cdwater.cdticket.admin.service.FileService;
import com.cdwater.cdticket.admin.common.Result;
import com.cdwater.cdticket.admin.dto.file.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final AdminAuthorizer adminAuthorizer;

    @PostMapping
    public Result<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        adminAuthorizer.requirePlatformAdmin();
        return Result.success(fileService.upload(file));
    }
}
