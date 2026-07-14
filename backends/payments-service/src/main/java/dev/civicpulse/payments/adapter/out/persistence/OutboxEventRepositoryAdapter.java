package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.application.port.out.OutboxEventRepository;
import dev.civicpulse.payments.domain.model.OutboxEvent;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class OutboxEventRepositoryAdapter implements OutboxEventRepository {

  private final OutboxEventJpaRepository jpaRepository;

  OutboxEventRepositoryAdapter(OutboxEventJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public OutboxEvent save(OutboxEvent event) {
    var saved =
        jpaRepository.save(
            new OutboxEventJpaEntity(
                event.id(),
                event.aggregateType(),
                event.aggregateId(),
                event.eventType(),
                event.payload(),
                event.createdAt(),
                event.publishedAt().orElse(null)));
    return toDomain(saved);
  }

  @Override
  public List<OutboxEvent> findUnpublished(int limit) {
    return jpaRepository.findByPublishedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, limit)).stream()
        .map(OutboxEventRepositoryAdapter::toDomain)
        .toList();
  }

  private static OutboxEvent toDomain(OutboxEventJpaEntity entity) {
    return OutboxEvent.reconstitute(
        entity.getId(), entity.getAggregateType(), entity.getAggregateId(), entity.getEventType(), entity.getPayload(), entity.getCreatedAt(), entity.getPublishedAt());
  }
}
