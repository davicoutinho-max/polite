package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SocialShareJpaRepository extends JpaRepository<SocialShareJpaEntity, UUID> {

  List<SocialShareJpaEntity> findByPostId(UUID postId);
}
