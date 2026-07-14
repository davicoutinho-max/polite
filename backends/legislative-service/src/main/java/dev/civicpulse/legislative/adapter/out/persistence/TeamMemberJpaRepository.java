package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TeamMemberJpaRepository extends JpaRepository<TeamMemberJpaEntity, UUID> {

  List<TeamMemberJpaEntity> findByPoliticianAccountId(UUID politicianAccountId);
}
