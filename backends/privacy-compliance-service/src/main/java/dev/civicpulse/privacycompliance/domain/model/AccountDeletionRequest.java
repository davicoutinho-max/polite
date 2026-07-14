package dev.civicpulse.privacycompliance.domain.model;

import dev.civicpulse.privacycompliance.domain.exception.InvalidDeletionTransitionException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** The account-erasure saga's aggregate root. No framework imports — the domain core of the
 * hexagonal architecture (see docs/architecture/system-architecture.html). */
public final class AccountDeletionRequest {

  private final UUID id;
  private final UUID accountId;
  private DeletionStatus status;
  private final Instant requestedAt;
  private Instant completedAt;

  private AccountDeletionRequest(UUID id, UUID accountId, DeletionStatus status, Instant requestedAt, Instant completedAt) {
    this.id = Objects.requireNonNull(id);
    this.accountId = Objects.requireNonNull(accountId);
    this.status = Objects.requireNonNull(status);
    this.requestedAt = Objects.requireNonNull(requestedAt);
    this.completedAt = completedAt;
  }

  public static AccountDeletionRequest request(UUID id, UUID accountId, Instant now) {
    return new AccountDeletionRequest(id, accountId, DeletionStatus.PENDING, now, null);
  }

  public static AccountDeletionRequest reconstitute(
      UUID id, UUID accountId, DeletionStatus status, Instant requestedAt, Instant completedAt) {
    return new AccountDeletionRequest(id, accountId, status, requestedAt, completedAt);
  }

  public void confirm() {
    requireStatus(DeletionStatus.PENDING, DeletionStatus.CONFIRMED);
    this.status = DeletionStatus.CONFIRMED;
  }

  public void startProcessing() {
    requireStatus(DeletionStatus.CONFIRMED, DeletionStatus.PROCESSING);
    this.status = DeletionStatus.PROCESSING;
  }

  public void complete(Instant now) {
    requireStatus(DeletionStatus.PROCESSING, DeletionStatus.COMPLETED);
    this.status = DeletionStatus.COMPLETED;
    this.completedAt = now;
  }

  public void cancel() {
    if (status != DeletionStatus.PENDING && status != DeletionStatus.CONFIRMED) {
      throw new InvalidDeletionTransitionException(status, DeletionStatus.CANCELED);
    }
    this.status = DeletionStatus.CANCELED;
  }

  private void requireStatus(DeletionStatus expected, DeletionStatus target) {
    if (status != expected) {
      throw new InvalidDeletionTransitionException(status, target);
    }
  }

  public UUID id() {
    return id;
  }

  public UUID accountId() {
    return accountId;
  }

  public DeletionStatus status() {
    return status;
  }

  public Instant requestedAt() {
    return requestedAt;
  }

  public Optional<Instant> completedAt() {
    return Optional.ofNullable(completedAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AccountDeletionRequest other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
