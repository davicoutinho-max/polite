package dev.civicpulse.notification.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PushTokenJpaRepository extends JpaRepository<PushTokenJpaEntity, PushTokenId> {

  List<PushTokenJpaEntity> findByAccountId(UUID accountId);
}
