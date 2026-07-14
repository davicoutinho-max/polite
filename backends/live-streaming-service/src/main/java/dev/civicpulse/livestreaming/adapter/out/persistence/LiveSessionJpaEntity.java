package dev.civicpulse.livestreaming.adapter.out.persistence;

import dev.civicpulse.livestreaming.domain.model.LiveSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_sessions")
public class LiveSessionJpaEntity {

  @Id private UUID id;

  @Column(name = "host_account_id", nullable = false)
  private UUID hostAccountId;

  @Column(name = "post_id")
  private UUID postId;

  @Column(name = "video_id")
  private String videoId;

  @Column(name = "channel_id")
  private String channelId;

  @Column(nullable = false)
  private LiveSessionStatus status;

  @Column(name = "scheduled_for")
  private Instant scheduledFor;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "ended_at")
  private Instant endedAt;

  @Column(name = "peak_viewers", nullable = false)
  private int peakViewers;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected LiveSessionJpaEntity() {}

  public LiveSessionJpaEntity(
      UUID id,
      UUID hostAccountId,
      UUID postId,
      String videoId,
      String channelId,
      LiveSessionStatus status,
      Instant scheduledFor,
      Instant startedAt,
      Instant endedAt,
      int peakViewers,
      Instant createdAt) {
    this.id = id;
    this.hostAccountId = hostAccountId;
    this.postId = postId;
    this.videoId = videoId;
    this.channelId = channelId;
    this.status = status;
    this.scheduledFor = scheduledFor;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.peakViewers = peakViewers;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getHostAccountId() {
    return hostAccountId;
  }

  public UUID getPostId() {
    return postId;
  }

  public String getVideoId() {
    return videoId;
  }

  public String getChannelId() {
    return channelId;
  }

  public LiveSessionStatus getStatus() {
    return status;
  }

  public Instant getScheduledFor() {
    return scheduledFor;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public Instant getEndedAt() {
    return endedAt;
  }

  public int getPeakViewers() {
    return peakViewers;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
