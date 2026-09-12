package com.cdwater.cdticket.common.storage;

import com.cdwater.cdticket.common.exception.BizException;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class MinioFileServiceTest {

    private MinioClient client;
    private MinioProperties props;
    private MinioFileService service;

    @BeforeEach
    void setUp() {
        client = org.mockito.Mockito.mock(MinioClient.class);
        props = new MinioProperties();
        props.setEndpoint("http://localhost:9000");
        props.setBucket("cd-ticket");
        service = new MinioFileService(client, props);
    }

    @Test
    void uploadReturnsPublicUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg",
                new byte[]{1, 2, 3});
        String url = service.upload(file);
        assertTrue(url.startsWith("http://localhost:9000/cd-ticket/"));
        assertTrue(url.endsWith(".jpg"));
    }

    @Test
    void uploadRejectsNonImage() {
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", new byte[]{1});
        assertThrows(BizException.class, () -> service.upload(file));
    }
}
