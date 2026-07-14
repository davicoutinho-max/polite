package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.domain.model.DeletionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_deletion_requests")
public class AccountDeletionRequestJpaEntity {

  @Id private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(nullable = false)
  private DeletionStatus status;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  protected AccountDeletionRequestJpaEntity() {}

  public AccountDeletionRequestJpaEntity(UUID id, UUID accountId, DeletionStatus status, Instant requestedAt, Instant completedAt) {
    this.id = id;
    this.accountId = accountId;
    this.status = status;
    this.requestedAt = requestedAt;
    this.completedAt = completedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public DeletionStatus getStatus() {
    return status;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }
}
