package dev.civicpulse.livestreaming.adapter.in.web.dto;

import dev.civicpulse.livestreaming.domain.model.LiveSession;
import java.time.Instant;
import java.util.UUID;

public record LiveSessionResponse(
    UUID id,
    UUID hostAccountId,
    UUID postId,
    String videoId,
    String channelId,
    String status,
    Instant scheduledFor,
    Instant startedAt,
    Instant endedAt,
    int peakViewers,
    Instant createdAt) {

  public static LiveSessionResponse from(LiveSession session) {
    return new LiveSessionResponse(
        session.id(),
        session.hostAccountId(),
        session.postId().orElse(null),
        session.videoId().orElse(null),
        session.channelId().orElse(null),
        session.status().code(),
        session.scheduledFor().orElse(null),
        session.startedAt().orElse(null),
        session.endedAt().orElse(null),
        session.peakViewers(),
        session.createdAt());
  }
}
