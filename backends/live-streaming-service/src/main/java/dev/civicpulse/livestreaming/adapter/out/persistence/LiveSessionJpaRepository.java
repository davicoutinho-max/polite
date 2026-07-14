package dev.civicpulse.livestreaming.adapter.out.persistence;

import dev.civicpulse.livestreaming.domain.model.LiveSessionStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface LiveSessionJpaRepository extends JpaRepository<LiveSessionJpaEntity, UUID> {

  List<LiveSessionJpaEntity> findByStatus(LiveSessionStatus status);
}
