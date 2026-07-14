package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SocialLinkJpaRepository extends JpaRepository<SocialLinkJpaEntity, UUID> {

  List<SocialLinkJpaEntity> findByPoliticianAccountId(UUID politicianAccountId);
}
