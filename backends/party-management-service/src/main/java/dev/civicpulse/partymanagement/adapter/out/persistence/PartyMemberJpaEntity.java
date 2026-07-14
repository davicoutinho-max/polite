package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.PartyMemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "party_members")
public class PartyMemberJpaEntity {

  @Id private UUID id;

  @Column(name = "party_id", nullable = false)
  private UUID partyId;

  @Column(name = "citizen_account_id", nullable = false)
  private UUID citizenAccountId;

  private String city;

  @Column(nullable = false)
  private PartyMemberStatus status;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt;

  protected PartyMemberJpaEntity() {}

  public PartyMemberJpaEntity(UUID id, UUID partyId, UUID citizenAccountId, String city, PartyMemberStatus status, Instant joinedAt) {
    this.id = id;
    this.partyId = partyId;
    this.citizenAccountId = citizenAccountId;
    this.city = city;
    this.status = status;
    this.joinedAt = joinedAt;
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

  public PartyMemberStatus getStatus() {
    return status;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }
}
