package dev.civicpulse.livestreaming.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Post-session rollup, written once when a session transitions to ended — never updated during
 * the live window (see schema.sql). */
public final class LiveSessionStats {

  private final UUID liveSessionId;
  private final int totalUniqueViewers;
  private final int totalChatMessages;
  private final Integer avgWatchSeconds;
  private final Instant computedAt;

  private LiveSessionStats(UUID liveSessionId, int totalUniqueViewers, int totalChatMessages, Integer avgWatchSeconds, Instant computedAt) {
    this.liveSessionId = Objects.requireNonNull(liveSessionId);
    this.totalUniqueViewers = totalUniqueViewers;
    this.totalChatMessages = totalChatMessages;
    this.avgWatchSeconds = avgWatchSeconds;
    this.computedAt = Objects.requireNonNull(computedAt);
  }

  public static LiveSessionStats compute(UUID liveSessionId, int totalUniqueViewers, int totalChatMessages, Integer avgWatchSeconds, Instant now) {
    return new LiveSessionStats(liveSessionId, totalUniqueViewers, totalChatMessages, avgWatchSeconds, now);
  }

  public static LiveSessionStats reconstitute(
      UUID liveSessionId, int totalUniqueViewers, int totalChatMessages, Integer avgWatchSeconds, Instant computedAt) {
    return new LiveSessionStats(liveSessionId, totalUniqueViewers, totalChatMessages, avgWatchSeconds, computedAt);
  }

  public UUID liveSessionId() {
    return liveSessionId;
  }

  public int totalUniqueViewers() {
    return totalUniqueViewers;
  }

  public int totalChatMessages() {
    return totalChatMessages;
  }

  public Optional<Integer> avgWatchSeconds() {
    return Optional.ofNullable(avgWatchSeconds);
  }

  public Instant computedAt() {
    return computedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LiveSessionStats other)) return false;
    return liveSessionId.equals(other.liveSessionId);
  }

  @Override
  public int hashCode() {
    return liveSessionId.hashCode();
  }
}
