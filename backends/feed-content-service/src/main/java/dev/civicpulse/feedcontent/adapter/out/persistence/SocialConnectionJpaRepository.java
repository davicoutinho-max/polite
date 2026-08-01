package dev.civicpulse.feedcontent.adapter.out.persistence;

import dev.civicpulse.feedcontent.domain.model.SocialPlatform;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SocialConnectionJpaRepository extends JpaRepository<SocialConnectionJpaEntity, UUID> {

  Optional<SocialConnectionJpaEntity> findByAccountIdAndPlatform(UUID accountId, SocialPlatform platform);

  List<SocialConnectionJpaEntity> findByAccountId(UUID accountId);

  void deleteByAccountIdAndPlatform(UUID accountId, SocialPlatform platform);
}
