package dev.civicpulse.notification.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

  @Query("select n from NotificationJpaEntity n where n.recipientAccountId = :recipientAccountId order by n.createdAt desc")
  List<NotificationJpaEntity> findByRecipientAccountId(@Param("recipientAccountId") UUID recipientAccountId, Pageable pageable);

  long countByRecipientAccountIdAndReadFalse(UUID recipientAccountId);

  boolean existsByRecipientAccountIdAndSourceEventId(UUID recipientAccountId, String sourceEventId);
}
