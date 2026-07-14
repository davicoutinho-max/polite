package dev.civicpulse.messaging.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ConversationJpaRepository extends JpaRepository<ConversationJpaEntity, UUID> {

  @Query(
      "select c from ConversationJpaEntity c where c.id in :ids "
          + "order by c.lastMessageAt desc nulls last, c.createdAt desc")
  List<ConversationJpaEntity> findByIdInOrdered(@Param("ids") List<UUID> ids);

  @Query(
      "select c from ConversationJpaEntity c where c.group = false "
          + "and c.id in (select p1.conversationId from ConversationParticipantJpaEntity p1 where p1.accountId = :accountA) "
          + "and c.id in (select p2.conversationId from ConversationParticipantJpaEntity p2 where p2.accountId = :accountB)")
  List<ConversationJpaEntity> findDirectBetween(@Param("accountA") UUID accountA, @Param("accountB") UUID accountB);
}
