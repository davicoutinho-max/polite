package dev.civicpulse.partymanagement.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "party_representatives")
public class PartyRepresentativeJpaEntity {

  @Id private UUID id;

  @Column(name = "party_id", nullable = false)
  private UUID partyId;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  @Column(name = "role_title")
  private String roleTitle;

  @Column(name = "linked_at", nullable = false)
  private Instant linkedAt;

  protected PartyRepresentativeJpaEntity() {}

  public PartyRepresentativeJpaEntity(UUID id, UUID partyId, UUID politicianAccountId, String roleTitle, Instant linkedAt) {
    this.id = id;
    this.partyId = partyId;
    this.politicianAccountId = politicianAccountId;
    this.roleTitle = roleTitle;
    this.linkedAt = linkedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPartyId() {
    return partyId;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public String getRoleTitle() {
    return roleTitle;
  }

  public Instant getLinkedAt() {
    return linkedAt;
  }
}
