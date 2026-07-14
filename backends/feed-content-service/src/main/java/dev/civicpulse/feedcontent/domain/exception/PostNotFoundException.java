package dev.civicpulse.feedcontent.domain.exception;

import java.util.UUID;

public final class PostNotFoundException extends RuntimeException {

  public PostNotFoundException(UUID id) {
    super("No post found with id " + id);
  }
}
