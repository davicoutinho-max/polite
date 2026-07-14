package dev.civicpulse.directory.domain.event;

import java.time.Instant;
import java.util.UUID;

public record FollowRemoved(UUID followerAccountId, String targetType, UUID targetId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "follow-removed";
  }
}
