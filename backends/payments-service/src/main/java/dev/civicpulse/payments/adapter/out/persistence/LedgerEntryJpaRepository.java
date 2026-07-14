package dev.civicpulse.payments.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryJpaEntity, Long> {

  Optional<LedgerEntryJpaEntity> findFirstByAccountIdOrderByIdDesc(UUID accountId);

  List<LedgerEntryJpaEntity> findByAccountIdOrderByIdAsc(UUID accountId);
}
