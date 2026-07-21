package dev.civicpulse.feedcontent.domain.exception;

import java.util.UUID;

public final class NotPostOwnerException extends RuntimeException {

  public NotPostOwnerException(UUID postId) {
    super("Only the author of post " + postId + " may delete it");
  }
}
