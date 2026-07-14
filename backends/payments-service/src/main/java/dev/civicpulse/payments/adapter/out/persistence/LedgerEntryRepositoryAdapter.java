package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.application.port.out.LedgerEntryRepository;
import dev.civicpulse.payments.domain.model.LedgerEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class LedgerEntryRepositoryAdapter implements LedgerEntryRepository {

  private final LedgerEntryJpaRepository jpaRepository;

  LedgerEntryRepositoryAdapter(LedgerEntryJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public LedgerEntry save(LedgerEntry entry) {
    var saved =
        jpaRepository.save(
            new LedgerEntryJpaEntity(
                entry.id().orElse(null),
                entry.paymentIntentId(),
                entry.accountId(),
                entry.direction(),
                entry.amountCents(),
                entry.runningBalanceCents(),
                entry.createdAt()));
    return toDomain(saved);
  }

  @Override
  public long currentBalance(UUID accountId) {
    return jpaRepository.findFirstByAccountIdOrderByIdDesc(accountId).map(LedgerEntryJpaEntity::getRunningBalanceCents).orElse(0L);
  }

  @Override
  public List<LedgerEntry> findByAccountId(UUID accountId) {
    return jpaRepository.findByAccountIdOrderByIdAsc(accountId).stream().map(LedgerEntryRepositoryAdapter::toDomain).toList();
  }

  private static LedgerEntry toDomain(LedgerEntryJpaEntity entity) {
    return LedgerEntry.reconstitute(
        entity.getId(), entity.getPaymentIntentId(), entity.getAccountId(), entity.getDirection(), entity.getAmountCents(), entity.getRunningBalanceCents(), entity.getCreatedAt());
  }
}
