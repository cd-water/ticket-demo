package com.cdwater.cdticket.admin.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    /**
     * 匿名只读策略模板，%s 处填 bucket 名
     */
    private static final String PUBLIC_READ_POLICY = """
            {
              "Version": "2012-10-17",
              "Statement": [{
                "Effect": "Allow",
                "Principal": {"AWS": ["*"]},
                "Action": ["s3:GetObject"],
                "Resource": ["arn:aws:s3:::%s/*"]
              }]
            }
            """;

    private final MinioProperties props;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
        initBucket(client);
        return client;
    }

    /**
     * 启动时确保 bucket 存在，并挂上匿名只读策略。
     */
    private void initBucket(MinioClient client) {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(props.getBucket()).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(props.getBucket()).build());
                log.info("minio bucket created: {}", props.getBucket());
            }
            client.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(props.getBucket())
                    .config(PUBLIC_READ_POLICY.formatted(props.getBucket()))
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("MinIO bucket 初始化失败", e);
        }
    }
}
