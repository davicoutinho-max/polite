package dev.civicpulse.elections.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "election_candidacies")
@IdClass(ElectionCandidacyId.class)
public class ElectionCandidacyJpaEntity {

  @Id
  @Column(name = "election_id")
  private UUID electionId;

  @Id
  @Column(name = "politician_account_id")
  private UUID politicianAccountId;

  protected ElectionCandidacyJpaEntity() {}

  public ElectionCandidacyJpaEntity(UUID electionId, UUID politicianAccountId) {
    this.electionId = electionId;
    this.politicianAccountId = politicianAccountId;
  }

  public UUID getElectionId() {
    return electionId;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }
}
