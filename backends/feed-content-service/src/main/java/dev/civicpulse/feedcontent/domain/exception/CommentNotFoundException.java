package dev.civicpulse.feedcontent.domain.exception;

import java.util.UUID;

public final class CommentNotFoundException extends RuntimeException {

  public CommentNotFoundException(UUID id) {
    super("No comment found with id " + id);
  }
}
