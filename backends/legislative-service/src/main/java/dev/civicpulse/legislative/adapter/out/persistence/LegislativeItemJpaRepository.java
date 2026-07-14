package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface LegislativeItemJpaRepository extends JpaRepository<LegislativeItemJpaEntity, UUID> {

  List<LegislativeItemJpaEntity> findByPoliticianAccountIdOrderByItemDateDesc(UUID politicianAccountId);

  List<LegislativeItemJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
