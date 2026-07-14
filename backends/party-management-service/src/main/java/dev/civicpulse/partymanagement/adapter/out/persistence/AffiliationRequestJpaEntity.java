package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.AffiliationRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "affiliation_requests")
public class AffiliationRequestJpaEntity {

  @Id private UUID id;

  @Column(name = "party_id", nullable = false)
  private UUID partyId;

  @Column(name = "citizen_account_id", nullable = false)
  private UUID citizenAccountId;

  private String city;

  @Column(nullable = false)
  private AffiliationRequestStatus status;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  @Column(name = "decided_at")
  private Instant decidedAt;

  protected AffiliationRequestJpaEntity() {}

  public AffiliationRequestJpaEntity(
      UUID id, UUID partyId, UUID citizenAccountId, String city, AffiliationRequestStatus status, Instant requestedAt, Instant decidedAt) {
    this.id = id;
    this.partyId = partyId;
    this.citizenAccountId = citizenAccountId;
    this.city = city;
    this.status = status;
    this.requestedAt = requestedAt;
    this.decidedAt = decidedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPartyId() {
    return partyId;
  }

  public UUID getCitizenAccountId() {
    return citizenAccountId;
  }

  public String getCity() {
    return city;
  }

  public AffiliationRequestStatus getStatus() {
    return status;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public Instant getDecidedAt() {
    return decidedAt;
  }
}
