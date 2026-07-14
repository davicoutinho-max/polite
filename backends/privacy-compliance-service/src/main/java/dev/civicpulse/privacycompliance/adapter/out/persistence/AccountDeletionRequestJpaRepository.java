package dev.civicpulse.privacycompliance.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AccountDeletionRequestJpaRepository extends JpaRepository<AccountDeletionRequestJpaEntity, UUID> {

  List<AccountDeletionRequestJpaEntity> findByAccountIdOrderByRequestedAtDesc(UUID accountId);
}
