package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface MandateJpaRepository extends JpaRepository<MandateJpaEntity, UUID> {

  List<MandateJpaEntity> findByPoliticianAccountId(UUID politicianAccountId);
}
