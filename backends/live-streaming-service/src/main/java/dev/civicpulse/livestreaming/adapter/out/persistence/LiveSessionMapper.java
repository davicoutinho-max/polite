package dev.civicpulse.livestreaming.adapter.out.persistence;

import dev.civicpulse.livestreaming.domain.model.LiveSession;
import org.springframework.stereotype.Component;

@Component
class LiveSessionMapper {

  LiveSession toDomain(LiveSessionJpaEntity entity) {
    return LiveSession.reconstitute(
        entity.getId(),
        entity.getHostAccountId(),
        entity.getPostId(),
        entity.getVideoId(),
        entity.getChannelId(),
        entity.getStatus(),
        entity.getScheduledFor(),
        entity.getStartedAt(),
        entity.getEndedAt(),
        entity.getPeakViewers(),
        entity.getCreatedAt());
  }

  LiveSessionJpaEntity toEntity(LiveSession session) {
    return new LiveSessionJpaEntity(
        session.id(),
        session.hostAccountId(),
        session.postId().orElse(null),
        session.videoId().orElse(null),
        session.channelId().orElse(null),
        session.status(),
        session.scheduledFor().orElse(null),
        session.startedAt().orElse(null),
        session.endedAt().orElse(null),
        session.peakViewers(),
        session.createdAt());
  }
}
