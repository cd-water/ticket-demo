package com.cdwater.cdticket.admin.interfaces;

import com.cdwater.cdticket.admin.application.dto.UploadResponse;
import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.api.Result;
import com.cdwater.cdticket.common.storage.MinioFileService;
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

    private final MinioFileService minioFileService;
    private final AdminAuthorizer adminAuthorizer;

    @PostMapping
    public Result<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        adminAuthorizer.requireSuperAdmin();
        UploadResponse resp = new UploadResponse();
        resp.setUrl(minioFileService.upload(file));
        return Result.success(resp);
    }
}
