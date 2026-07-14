package dev.civicpulse.directory.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Feed &amp; Content (to populate the "Following" feed sort) and by Notification
 * (to notify the followed politician/party). */
public record FollowCreated(UUID followerAccountId, String targetType, UUID targetId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "follow-created";
  }
}
