package dev.civicpulse.activityfeed.adapter.out.persistence;

import dev.civicpulse.activityfeed.domain.model.TimelineEvent;
import org.springframework.stereotype.Component;

@Component
class TimelineEventMapper {

  TimelineEvent toDomain(TimelineEventJpaEntity entity) {
    return TimelineEvent.reconstitute(
        entity.getId(),
        entity.getSubjectAccountId(),
        entity.getType(),
        entity.getTitle(),
        entity.getDetail(),
        entity.getOccurredAt(),
        entity.getSourceEventId(),
        entity.getActorAccountId(),
        entity.getActorNameDenormalized(),
        entity.getCreatedAt());
  }

  TimelineEventJpaEntity toEntity(TimelineEvent event) {
    return new TimelineEventJpaEntity(
        event.id().orElse(null),
        event.subjectAccountId(),
        event.type(),
        event.title(),
        event.detail().orElse(null),
        event.occurredAt(),
        event.sourceEventId(),
        event.actorAccountId(),
        event.actorNameDenormalized().orElse(null),
        event.createdAt());
  }
}
