package dev.civicpulse.analytics.adapter.out.persistence;

import dev.civicpulse.analytics.domain.model.EngagementEvent;
import org.springframework.stereotype.Component;

@Component
class EngagementEventMapper {

  EngagementEvent toDomain(EngagementEventJpaEntity entity) {
    return EngagementEvent.reconstitute(
        entity.getId(),
        entity.getAuthorAccountId(),
        entity.getActorAccountId(),
        entity.getActorAccountType(),
        entity.getEventType(),
        entity.getContentType(),
        entity.getOccurredAt(),
        entity.getSourceEventId(),
        entity.getCreatedAt());
  }

  EngagementEventJpaEntity toEntity(EngagementEvent event) {
    return new EngagementEventJpaEntity(
        event.id().orElse(null),
        event.authorAccountId(),
        event.actorAccountId(),
        event.actorAccountType().orElse(null),
        event.eventType(),
        event.contentType().orElse(null),
        event.occurredAt(),
        event.sourceEventId(),
        event.createdAt());
  }
}
