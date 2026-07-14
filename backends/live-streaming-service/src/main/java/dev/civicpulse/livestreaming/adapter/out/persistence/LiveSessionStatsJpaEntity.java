package dev.civicpulse.livestreaming.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_session_stats")
public class LiveSessionStatsJpaEntity {

  @Id
  @Column(name = "live_session_id")
  private UUID liveSessionId;

  @Column(name = "total_unique_viewers", nullable = false)
  private int totalUniqueViewers;

  @Column(name = "total_chat_messages", nullable = false)
  private int totalChatMessages;

  @Column(name = "avg_watch_seconds")
  private Integer avgWatchSeconds;

  @Column(name = "computed_at", nullable = false)
  private Instant computedAt;

  protected LiveSessionStatsJpaEntity() {}

  public LiveSessionStatsJpaEntity(UUID liveSessionId, int totalUniqueViewers, int totalChatMessages, Integer avgWatchSeconds, Instant computedAt) {
    this.liveSessionId = liveSessionId;
    this.totalUniqueViewers = totalUniqueViewers;
    this.totalChatMessages = totalChatMessages;
    this.avgWatchSeconds = avgWatchSeconds;
    this.computedAt = computedAt;
  }

  public UUID getLiveSessionId() {
    return liveSessionId;
  }

  public int getTotalUniqueViewers() {
    return totalUniqueViewers;
  }

  public int getTotalChatMessages() {
    return totalChatMessages;
  }

  public Integer getAvgWatchSeconds() {
    return avgWatchSeconds;
  }

  public Instant getComputedAt() {
    return computedAt;
  }
}
