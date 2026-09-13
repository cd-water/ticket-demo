package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.config.MinioProperties;
import com.cdwater.cdticket.admin.dto.file.UploadResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final MinioClient minioClient;
    private final MinioProperties props;

    public UploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片", ResultCode.BAD_REQUEST.getCode());
        }
        String ext = extOf(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXT.contains(ext)) {
            throw new BizException("仅支持 jpg/jpeg/png/webp/gif 图片", ResultCode.BAD_REQUEST.getCode());
        }
        String object = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "/" + UUID.randomUUID() + "." + ext;
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(object)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            log.error("minio upload failed", e);
            throw new BizException("图片上传失败，请稍后重试", ResultCode.INTERNAL_ERROR.getCode());
        }
        UploadResponse resp = new UploadResponse();
        resp.setUrl(props.getEndpoint() + "/" + props.getBucket() + "/" + object);
        return resp;
    }

    private String extOf(String name) {
        if (name == null) return null;
        int i = name.lastIndexOf('.');
        return i < 0 ? null : name.substring(i + 1).toLowerCase(Locale.ROOT);
    }
}
