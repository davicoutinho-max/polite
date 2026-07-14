package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.domain.model.LedgerDirection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "payment_intent_id", nullable = false)
  private UUID paymentIntentId;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(nullable = false)
  private LedgerDirection direction;

  @Column(name = "amount_cents", nullable = false)
  private long amountCents;

  @Column(name = "running_balance_cents", nullable = false)
  private long runningBalanceCents;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected LedgerEntryJpaEntity() {}

  public LedgerEntryJpaEntity(
      Long id, UUID paymentIntentId, UUID accountId, LedgerDirection direction, long amountCents, long runningBalanceCents, Instant createdAt) {
    this.id = id;
    this.paymentIntentId = paymentIntentId;
    this.accountId = accountId;
    this.direction = direction;
    this.amountCents = amountCents;
    this.runningBalanceCents = runningBalanceCents;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public UUID getPaymentIntentId() {
    return paymentIntentId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public LedgerDirection getDirection() {
    return direction;
  }

  public long getAmountCents() {
    return amountCents;
  }

  public long getRunningBalanceCents() {
    return runningBalanceCents;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
