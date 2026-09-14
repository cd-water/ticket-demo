package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private static final Tika TIKA = new Tika();

    private static final Set<String> ALLOWED_MIME = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private static final Map<String, String> MIME_EXT = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");

    private final MinioClient minioClient;
    private final MinioProperties props;

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片", ResultCode.BAD_REQUEST.getCode());
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BizException("图片读取失败，请稍后重试", ResultCode.INTERNAL_ERROR.getCode());
        }
        String mime = TIKA.detect(bytes);
        if (!ALLOWED_MIME.contains(mime)) {
            throw new BizException("仅支持 jpg/jpeg/png/webp/gif 图片", ResultCode.BAD_REQUEST.getCode());
        }
        String object = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "/" + UUID.randomUUID() + "." + MIME_EXT.get(mime);
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(object)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType(mime)
                    .build());
        } catch (Exception e) {
            log.error("minio upload failed", e);
            throw new BizException("图片上传失败，请稍后重试", ResultCode.INTERNAL_ERROR.getCode());
        }
        return props.getEndpoint() + "/" + props.getBucket() + "/" + object;
    }
}
