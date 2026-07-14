package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.PartyOfficeScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "party_offices")
public class PartyOfficeJpaEntity {

  @Id private UUID id;

  @Column(name = "party_id", nullable = false)
  private UUID partyId;

  @Column(nullable = false)
  private PartyOfficeScope scope;

  @Column(nullable = false)
  private String location;

  @Column(name = "leader_name")
  private String leaderName;

  @Column(name = "member_count", nullable = false)
  private int memberCount;

  protected PartyOfficeJpaEntity() {}

  public PartyOfficeJpaEntity(UUID id, UUID partyId, PartyOfficeScope scope, String location, String leaderName, int memberCount) {
    this.id = id;
    this.partyId = partyId;
    this.scope = scope;
    this.location = location;
    this.leaderName = leaderName;
    this.memberCount = memberCount;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPartyId() {
    return partyId;
  }

  public PartyOfficeScope getScope() {
    return scope;
  }

  public String getLocation() {
    return location;
  }

  public String getLeaderName() {
    return leaderName;
  }

  public int getMemberCount() {
    return memberCount;
  }
}
