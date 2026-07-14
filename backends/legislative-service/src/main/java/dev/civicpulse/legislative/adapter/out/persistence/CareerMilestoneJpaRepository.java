package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CareerMilestoneJpaRepository extends JpaRepository<CareerMilestoneJpaEntity, UUID> {

  List<CareerMilestoneJpaEntity> findByPoliticianAccountIdOrderByYear(UUID politicianAccountId);
}
