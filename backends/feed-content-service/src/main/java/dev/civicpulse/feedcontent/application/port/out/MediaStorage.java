package dev.civicpulse.feedcontent.application.port.out;

import java.io.InputStream;

/** Object storage for post attachments (images and generic files) — backed by MinIO, see
 * MinioMediaStorageAdapter. */
public interface MediaStorage {

  /** Stores the stream under a generated key and returns its publicly reachable URL. */
  String upload(String originalFileName, String contentType, InputStream data, long size);
}
