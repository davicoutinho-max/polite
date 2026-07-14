package dev.civicpulse.payments.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Append-only — the source of truth behind every "public ledger" bar shown in Fundraising.
 * {@code id} is a DB-generated identity column, so it's {@code null} until persisted. */
public final class LedgerEntry {

  private final Long id;
  private final UUID paymentIntentId;
  private final UUID accountId;
  private final LedgerDirection direction;
  private final long amountCents;
  private final long runningBalanceCents;
  private final Instant createdAt;

  private LedgerEntry(
      Long id, UUID paymentIntentId, UUID accountId, LedgerDirection direction, long amountCents, long runningBalanceCents, Instant createdAt) {
    this.id = id;
    this.paymentIntentId = Objects.requireNonNull(paymentIntentId);
    this.accountId = Objects.requireNonNull(accountId);
    this.direction = Objects.requireNonNull(direction);
    this.amountCents = amountCents;
    this.runningBalanceCents = runningBalanceCents;
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static LedgerEntry record(
      UUID paymentIntentId, UUID accountId, LedgerDirection direction, long amountCents, long runningBalanceCents, Instant now) {
    return new LedgerEntry(null, paymentIntentId, accountId, direction, amountCents, runningBalanceCents, now);
  }

  public static LedgerEntry reconstitute(
      Long id, UUID paymentIntentId, UUID accountId, LedgerDirection direction, long amountCents, long runningBalanceCents, Instant createdAt) {
    return new LedgerEntry(id, paymentIntentId, accountId, direction, amountCents, runningBalanceCents, createdAt);
  }

  public Optional<Long> id() {
    return Optional.ofNullable(id);
  }

  public UUID paymentIntentId() {
    return paymentIntentId;
  }

  public UUID accountId() {
    return accountId;
  }

  public LedgerDirection direction() {
    return direction;
  }

  public long amountCents() {
    return amountCents;
  }

  public long runningBalanceCents() {
    return runningBalanceCents;
  }

  public Instant createdAt() {
    return createdAt;
  }
}
