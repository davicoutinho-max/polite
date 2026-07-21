package dev.civicpulse.platformconfig.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PoliticalPositionJpaRepository extends JpaRepository<PoliticalPositionJpaEntity, UUID> {

  List<PoliticalPositionJpaEntity> findAllByOrderBySortOrder();
}
