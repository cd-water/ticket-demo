package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.ObjectWriteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    /** 1x1 透明 PNG */
    private static final byte[] PNG_BYTES = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");

    @Mock
    private MinioClient minioClient;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        MinioProperties props = new MinioProperties();
        props.setEndpoint("http://localhost:9000");
        props.setAccessKey("cdwaterminio");
        props.setSecretKey("cdwaterminio");
        props.setBucket("cd-ticket");
        fileService = new FileService(minioClient, props);
    }

    @Test
    void upload_nullFile_throwsBadRequest() {
        assertThatThrownBy(() -> fileService.upload(null))
                .isInstanceOf(BizException.class)
                .hasMessage("请选择要上传的图片")
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void upload_emptyFile_throwsBadRequest() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> fileService.upload(file))
                .isInstanceOf(BizException.class)
                .hasMessage("请选择要上传的图片");
    }

    @Test
    void upload_readFailure_throwsInternalError() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("read failed"));

        assertThatThrownBy(() -> fileService.upload(file))
                .isInstanceOf(BizException.class)
                .hasMessage("图片读取失败，请稍后重试")
                .extracting("code")
                .isEqualTo(500);
    }

    @Test
    void upload_disallowedMime_throwsBadRequest() {
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain",
                "hello".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> fileService.upload(file))
                .isInstanceOf(BizException.class)
                .hasMessage("仅支持 jpg/jpeg/png/webp/gif 图片")
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void upload_success_putsObjectAndReturnsUrl() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", PNG_BYTES);

        String url = fileService.upload(file);

        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        assertThat(url).startsWith("http://localhost:9000/cd-ticket/" + dateDir + "/").endsWith(".png");

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        PutObjectArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo("cd-ticket");
        assertThat(args.contentType()).isEqualTo("image/png");
        assertThat(args.object()).startsWith(dateDir + "/").endsWith(".png");
    }

    @Test
    void upload_minioFailure_throwsInternalError() throws Exception {
        doThrow(new RuntimeException("minio down"))
                .when(minioClient).putObject(any(PutObjectArgs.class));
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", PNG_BYTES);

        assertThatThrownBy(() -> fileService.upload(file))
                .isInstanceOf(BizException.class)
                .hasMessage("图片上传失败，请稍后重试")
                .extracting("code")
                .isEqualTo(500);
    }
}
