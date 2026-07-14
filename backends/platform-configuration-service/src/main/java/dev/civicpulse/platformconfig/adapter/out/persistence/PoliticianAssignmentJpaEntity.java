package dev.civicpulse.platformconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "politician_assignments")
public class PoliticianAssignmentJpaEntity {

  @Id
  @Column(name = "politician_account_id")
  private UUID politicianAccountId;

  @Column(name = "party_id", nullable = false)
  private UUID partyId;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PoliticianAssignmentJpaEntity() {}

  public PoliticianAssignmentJpaEntity(UUID politicianAccountId, UUID partyId, Instant updatedAt) {
    this.politicianAccountId = politicianAccountId;
    this.partyId = partyId;
    this.updatedAt = updatedAt;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public UUID getPartyId() {
    return partyId;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
