package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.CommitteeKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "committee_memberships")
public class CommitteeMembershipJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String role;

  @Column(nullable = false)
  private CommitteeKind kind;

  protected CommitteeMembershipJpaEntity() {}

  public CommitteeMembershipJpaEntity(UUID id, UUID politicianAccountId, String name, String role, CommitteeKind kind) {
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.name = name;
    this.role = role;
    this.kind = kind;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public String getName() {
    return name;
  }

  public String getRole() {
    return role;
  }

  public CommitteeKind getKind() {
    return kind;
  }
}
