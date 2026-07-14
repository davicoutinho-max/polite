package dev.civicpulse.feedcontent.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PostLiked(UUID postId, UUID accountId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "post-liked";
  }
}
