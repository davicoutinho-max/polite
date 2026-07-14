package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, UUID> {

  List<CommentJpaEntity> findByPostIdOrderByCreatedAtAsc(UUID postId);
}
