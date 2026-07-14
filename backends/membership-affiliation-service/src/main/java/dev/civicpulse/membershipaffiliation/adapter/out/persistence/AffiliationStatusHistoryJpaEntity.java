package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatus;
import dev.civicpulse.membershipaffiliation.domain.model.ChangedBy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "affiliation_status_history")
public class AffiliationStatusHistoryJpaEntity {

  @Id private UUID id;

  @Column(name = "affiliation_id", nullable = false)
  private UUID affiliationId;

  @Column(name = "from_status")
  private AffiliationStatus fromStatus;

  @Column(name = "to_status", nullable = false)
  private AffiliationStatus toStatus;

  @Column(name = "changed_by", nullable = false)
  private ChangedBy changedBy;

  @Column(name = "changed_at", nullable = false)
  private Instant changedAt;

  protected AffiliationStatusHistoryJpaEntity() {}

  public AffiliationStatusHistoryJpaEntity(
      UUID id, UUID affiliationId, AffiliationStatus fromStatus, AffiliationStatus toStatus, ChangedBy changedBy, Instant changedAt) {
    this.id = id;
    this.affiliationId = affiliationId;
    this.fromStatus = fromStatus;
    this.toStatus = toStatus;
    this.changedBy = changedBy;
    this.changedAt = changedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAffiliationId() {
    return affiliationId;
  }

  public AffiliationStatus getFromStatus() {
    return fromStatus;
  }

  public AffiliationStatus getToStatus() {
    return toStatus;
  }

  public ChangedBy getChangedBy() {
    return changedBy;
  }

  public Instant getChangedAt() {
    return changedAt;
  }
}
