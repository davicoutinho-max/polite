package dev.civicpulse.feedcontent.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PostPublished(UUID postId, UUID authorId, String kind, String visibility, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "post-published";
  }
}
