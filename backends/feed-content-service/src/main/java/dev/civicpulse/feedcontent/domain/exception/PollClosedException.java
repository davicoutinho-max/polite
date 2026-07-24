package dev.civicpulse.feedcontent.domain.exception;

import java.util.UUID;

public final class PollClosedException extends RuntimeException {

  public PollClosedException(UUID postId) {
    super("Poll on post " + postId + " is closed — voting is no longer allowed");
  }
}
