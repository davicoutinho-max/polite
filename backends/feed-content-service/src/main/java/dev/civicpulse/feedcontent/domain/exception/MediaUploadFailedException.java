package dev.civicpulse.feedcontent.domain.exception;

public final class MediaUploadFailedException extends RuntimeException {

  public MediaUploadFailedException(Throwable cause) {
    super("Failed to upload media", cause);
  }
}
