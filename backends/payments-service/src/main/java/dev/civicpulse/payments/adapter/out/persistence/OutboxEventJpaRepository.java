package dev.civicpulse.payments.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

  List<OutboxEventJpaEntity> findByPublishedAtIsNullOrderByCreatedAtAsc(Pageable pageable);
}
