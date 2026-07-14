package dev.civicpulse.activityfeed.adapter.out.persistence;

import dev.civicpulse.activityfeed.application.port.out.TimelineEventRepository;
import dev.civicpulse.activityfeed.domain.model.TimelineEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class TimelineEventRepositoryAdapter implements TimelineEventRepository {

  private final TimelineEventJpaRepository jpaRepository;
  private final TimelineEventMapper mapper;

  TimelineEventRepositoryAdapter(TimelineEventJpaRepository jpaRepository, TimelineEventMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public TimelineEvent save(TimelineEvent event) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(event)));
  }

  @Override
  public boolean existsBySubjectAndSourceEventId(UUID subjectAccountId, String sourceEventId) {
    return jpaRepository.existsBySubjectAccountIdAndSourceEventId(subjectAccountId, sourceEventId);
  }

  @Override
  public List<TimelineEvent> findBySubject(UUID subjectAccountId, int limit) {
    return jpaRepository.findBySubjectAccountIdOrderByOccurredAtDesc(subjectAccountId, PageRequest.of(0, limit)).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
