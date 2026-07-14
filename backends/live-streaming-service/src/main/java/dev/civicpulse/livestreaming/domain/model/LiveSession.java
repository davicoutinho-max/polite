package dev.civicpulse.livestreaming.domain.model;

import dev.civicpulse.livestreaming.domain.exception.InvalidLiveSessionTransitionException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** The live-session aggregate: a low-volume, transactional lifecycle (scheduled -> live ->
 * ended) around what is otherwise a high-churn, Redis-backed presence/chat system (see
 * LiveStreamingServiceApplication's scope note). No framework imports — the domain core of the
 * hexagonal architecture (see docs/architecture/system-architecture.html). */
public final class LiveSession {

  private final UUID id;
  private final UUID hostAccountId;
  private UUID postId;
  private final String videoId;
  private final String channelId;
  private LiveSessionStatus status;
  private final Instant scheduledFor;
  private Instant startedAt;
  private Instant endedAt;
  private int peakViewers;
  private final Instant createdAt;

  private LiveSession(
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
    this.id = Objects.requireNonNull(id);
    this.hostAccountId = Objects.requireNonNull(hostAccountId);
    this.postId = postId;
    this.videoId = videoId;
    this.channelId = channelId;
    this.status = Objects.requireNonNull(status);
    this.scheduledFor = scheduledFor;
    this.startedAt = startedAt;
    this.endedAt = endedAt;
    this.peakViewers = peakViewers;
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static LiveSession schedule(UUID id, UUID hostAccountId, String videoId, String channelId, Instant scheduledFor, Instant now) {
    return new LiveSession(id, hostAccountId, null, videoId, channelId, LiveSessionStatus.SCHEDULED, scheduledFor, null, null, 0, now);
  }

  public static LiveSession reconstitute(
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
    return new LiveSession(id, hostAccountId, postId, videoId, channelId, status, scheduledFor, startedAt, endedAt, peakViewers, createdAt);
  }

  public void start(Instant now) {
    requireStatus(LiveSessionStatus.SCHEDULED, LiveSessionStatus.LIVE);
    this.status = LiveSessionStatus.LIVE;
    this.startedAt = now;
  }

  public void end(Instant now) {
    requireStatus(LiveSessionStatus.LIVE, LiveSessionStatus.ENDED);
    this.status = LiveSessionStatus.ENDED;
    this.endedAt = now;
  }

  public void attachPost(UUID postId) {
    this.postId = Objects.requireNonNull(postId);
  }

  /** Reported periodically by the client from Redis' live viewer-count key (see the service-level
   * scope note) — only meaningful while the session is actually live. */
  public void recordViewerCount(int currentViewers) {
    if (status != LiveSessionStatus.LIVE) {
      throw new InvalidLiveSessionTransitionException(status, LiveSessionStatus.LIVE);
    }
    this.peakViewers = Math.max(this.peakViewers, currentViewers);
  }

  private void requireStatus(LiveSessionStatus expected, LiveSessionStatus target) {
    if (status != expected) {
      throw new InvalidLiveSessionTransitionException(status, target);
    }
  }

  public UUID id() {
    return id;
  }

  public UUID hostAccountId() {
    return hostAccountId;
  }

  public Optional<UUID> postId() {
    return Optional.ofNullable(postId);
  }

  public Optional<String> videoId() {
    return Optional.ofNullable(videoId);
  }

  public Optional<String> channelId() {
    return Optional.ofNullable(channelId);
  }

  public LiveSessionStatus status() {
    return status;
  }

  public Optional<Instant> scheduledFor() {
    return Optional.ofNullable(scheduledFor);
  }

  public Optional<Instant> startedAt() {
    return Optional.ofNullable(startedAt);
  }

  public Optional<Instant> endedAt() {
    return Optional.ofNullable(endedAt);
  }

  public int peakViewers() {
    return peakViewers;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LiveSession other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
