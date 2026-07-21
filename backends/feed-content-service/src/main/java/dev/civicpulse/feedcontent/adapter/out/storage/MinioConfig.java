package dev.civicpulse.feedcontent.adapter.out.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
class MinioConfig {

  private final String bucket;
  private final MinioClient minioClient;

  MinioConfig(
      @Value("${minio.endpoint}") String endpoint,
      @Value("${minio.access-key}") String accessKey,
      @Value("${minio.secret-key}") String secretKey,
      @Value("${minio.bucket}") String bucket) {
    this.bucket = bucket;
    this.minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
  }

  @Bean
  MinioClient minioClient() {
    return minioClient;
  }

  /** Post attachments are only ever linked from public post responses, so the bucket is created
   * (idempotently) with an anonymous-read policy — simple direct URLs rather than presigned ones. */
  @EventListener(ApplicationReadyEvent.class)
  void ensureBucketExists() throws Exception {
    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
      String policy =
          """
          {
            "Version": "2012-10-17",
            "Statement": [{
              "Effect": "Allow",
              "Principal": {"AWS": ["*"]},
              "Action": ["s3:GetObject"],
              "Resource": ["arn:aws:s3:::%s/*"]
            }]
          }
          """
              .formatted(bucket);
      minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
    }
  }
}
