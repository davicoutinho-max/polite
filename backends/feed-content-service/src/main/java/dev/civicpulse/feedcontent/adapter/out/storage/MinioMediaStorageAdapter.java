package dev.civicpulse.feedcontent.adapter.out.storage;

import dev.civicpulse.feedcontent.application.port.out.MediaStorage;
import dev.civicpulse.feedcontent.domain.exception.MediaUploadFailedException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class MinioMediaStorageAdapter implements MediaStorage {

  private final MinioClient minioClient;
  private final String bucket;
  private final String publicUrl;

  MinioMediaStorageAdapter(MinioClient minioClient, @Value("${minio.bucket}") String bucket, @Value("${minio.public-url}") String publicUrl) {
    this.minioClient = minioClient;
    this.bucket = bucket;
    this.publicUrl = publicUrl;
  }

  @Override
  public String upload(String originalFileName, String contentType, InputStream data, long size) {
    String key = UUID.randomUUID() + "-" + sanitize(originalFileName);
    try {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucket).object(key).stream(data, size, -1).contentType(contentType).build());
    } catch (Exception e) {
      throw new MediaUploadFailedException(e);
    }
    return publicUrl + "/" + bucket + "/" + key;
  }

  private static String sanitize(String fileName) {
    String base = fileName == null || fileName.isBlank() ? "file" : fileName;
    return base.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
