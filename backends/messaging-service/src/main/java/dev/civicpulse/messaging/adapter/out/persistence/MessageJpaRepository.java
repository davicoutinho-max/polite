package dev.civicpulse.messaging.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, UUID> {

  // Newest-first (page 0 = most recent) — matches the "load older on scroll-up" chat pagination
  // pattern the frontend uses; it reverses each page for display and requests page+1 for history.
  @Query("select m from MessageJpaEntity m where m.conversationId = :conversationId order by m.createdAt desc")
  List<MessageJpaEntity> findByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);

  // Derived delete queries execute entity-by-entity via EntityManager#remove and — unlike other
  // Spring Data repository methods — do NOT get an implicit transaction; Spring Data's own docs
  // call this out explicitly. Without @Transactional here, calling this outside an existing
  // transaction throws TransactionRequiredException.
  @Transactional
  void deleteByConversationId(UUID conversationId);
}
