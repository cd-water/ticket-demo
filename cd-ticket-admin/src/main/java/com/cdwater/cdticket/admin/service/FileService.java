package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.dto.file.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioFileService minioFileService;

    public UploadResponse upload(MultipartFile file) {
        UploadResponse resp = new UploadResponse();
        resp.setUrl(minioFileService.upload(file));
        return resp;
    }
}
