package dev.civicpulse.feedcontent.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, UUID> {

  List<CommentJpaEntity> findByPostIdOrderByCreatedAtAsc(UUID postId);

  // Derived delete queries execute entity-by-entity via EntityManager#remove and — unlike other
  // Spring Data repository methods — do NOT get an implicit transaction; without @Transactional
  // here, calling this outside an existing transaction throws TransactionRequiredException.
  @Transactional
  void deleteByPostId(UUID postId);
}
