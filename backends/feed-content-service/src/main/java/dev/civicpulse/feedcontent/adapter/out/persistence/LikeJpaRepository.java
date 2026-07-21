package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

interface LikeJpaRepository extends JpaRepository<LikeJpaEntity, LikeId> {

  // See CommentJpaRepository's deleteByPostId for why @Transactional is required here.
  @Transactional
  void deleteByPostId(UUID postId);
}
