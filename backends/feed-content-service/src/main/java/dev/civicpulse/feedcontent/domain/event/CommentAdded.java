package dev.civicpulse.feedcontent.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CommentAdded(UUID postId, UUID commentId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "comment-added";
  }
}
