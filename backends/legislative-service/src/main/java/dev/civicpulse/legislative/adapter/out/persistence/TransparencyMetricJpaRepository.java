package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TransparencyMetricJpaRepository extends JpaRepository<TransparencyMetricJpaEntity, UUID> {

  List<TransparencyMetricJpaEntity> findByPoliticianAccountId(UUID politicianAccountId);
}
