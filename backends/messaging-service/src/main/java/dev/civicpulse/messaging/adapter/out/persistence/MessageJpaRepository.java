package dev.civicpulse.messaging.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, UUID> {

  @Query("select m from MessageJpaEntity m where m.conversationId = :conversationId order by m.createdAt asc")
  List<MessageJpaEntity> findByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);
}
