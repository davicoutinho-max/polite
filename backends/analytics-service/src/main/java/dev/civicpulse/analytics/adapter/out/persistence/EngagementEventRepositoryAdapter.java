package dev.civicpulse.analytics.adapter.out.persistence;

import dev.civicpulse.analytics.application.port.out.EngagementEventRepository;
import dev.civicpulse.analytics.domain.model.EngagementEvent;
import dev.civicpulse.analytics.domain.model.EngagementEventType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class EngagementEventRepositoryAdapter implements EngagementEventRepository {

  private final EngagementEventJpaRepository jpaRepository;
  private final EngagementEventMapper mapper;

  EngagementEventRepositoryAdapter(EngagementEventJpaRepository jpaRepository, EngagementEventMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public EngagementEvent save(EngagementEvent event) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(event)));
  }

  @Override
  public boolean existsByAuthorAndSourceEventId(UUID authorAccountId, String sourceEventId) {
    return jpaRepository.existsByAuthorAccountIdAndSourceEventId(authorAccountId, sourceEventId);
  }

  @Override
  public long countByAuthorAndType(UUID authorAccountId, String eventType) {
    return jpaRepository.countByAuthorAccountIdAndEventType(authorAccountId, EngagementEventType.fromCode(eventType));
  }

  @Override
  public long countDistinctActors(UUID authorAccountId, List<String> eventTypes) {
    return jpaRepository.countDistinctActors(authorAccountId, toTypes(eventTypes));
  }

  @Override
  public List<DailyCount> dailyLikeCommentCounts(UUID authorAccountId, Instant since) {
    return jpaRepository.dailyLikeCommentCounts(authorAccountId, since).stream()
        .map(p -> new DailyCount(p.getDay().toLocalDate(), p.getLikes(), p.getComments()))
        .toList();
  }

  @Override
  public List<TypeCount> countByContentType(UUID authorAccountId, List<String> eventTypes) {
    return jpaRepository.countByContentType(authorAccountId, toTypes(eventTypes)).stream()
        .map(p -> new TypeCount(p.getKey(), p.getCount()))
        .toList();
  }

  @Override
  public List<TypeCount> countByActorAccountType(UUID authorAccountId, List<String> eventTypes) {
    return jpaRepository.countByActorAccountType(authorAccountId, toTypes(eventTypes)).stream()
        .map(p -> new TypeCount(p.getKey(), p.getCount()))
        .toList();
  }

  private static List<EngagementEventType> toTypes(List<String> codes) {
    return codes.stream().map(EngagementEventType::fromCode).toList();
  }
}
