package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "affiliations")
public class AffiliationJpaEntity {

  @Id private UUID id;

  @Column(name = "citizen_account_id", nullable = false)
  private UUID citizenAccountId;

  @Column(name = "party_id", nullable = false)
  private UUID partyId;

  @Column(nullable = false)
  private AffiliationStatus status;

  @Column(name = "requested_at")
  private Instant requestedAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected AffiliationJpaEntity() {}

  public AffiliationJpaEntity(
      UUID id, UUID citizenAccountId, UUID partyId, AffiliationStatus status, Instant requestedAt, Instant updatedAt) {
    this.id = id;
    this.citizenAccountId = citizenAccountId;
    this.partyId = partyId;
    this.status = status;
    this.requestedAt = requestedAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getCitizenAccountId() {
    return citizenAccountId;
  }

  public UUID getPartyId() {
    return partyId;
  }

  public AffiliationStatus getStatus() {
    return status;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
