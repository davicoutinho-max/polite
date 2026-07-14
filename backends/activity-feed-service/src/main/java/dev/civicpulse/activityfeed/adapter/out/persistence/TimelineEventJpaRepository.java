package dev.civicpulse.activityfeed.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface TimelineEventJpaRepository extends JpaRepository<TimelineEventJpaEntity, UUID> {

  boolean existsBySubjectAccountIdAndSourceEventId(UUID subjectAccountId, String sourceEventId);

  java.util.List<TimelineEventJpaEntity> findBySubjectAccountIdOrderByOccurredAtDesc(UUID subjectAccountId, Pageable pageable);
}
