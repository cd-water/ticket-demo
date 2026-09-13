package com.cdwater.cdticket.admin.interfaces.controller;

import com.cdwater.cdticket.admin.application.AdminAuthorizer;
import com.cdwater.cdticket.admin.application.FileService;
import com.cdwater.cdticket.admin.application.dto.Result;
import com.cdwater.cdticket.admin.application.dto.file.UploadResponse;
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
        adminAuthorizer.requireSuperAdmin();
        return Result.success(fileService.upload(file));
    }
}
