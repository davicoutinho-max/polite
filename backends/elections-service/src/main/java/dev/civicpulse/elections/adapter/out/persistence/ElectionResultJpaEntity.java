package dev.civicpulse.elections.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "election_results")
public class ElectionResultJpaEntity {

  @Id private UUID id;

  @Column(name = "election_id")
  private UUID electionId;

  private String office;

  @Column(name = "external_id")
  private String externalId;

  @Column(name = "candidate_name")
  private String candidateName;

  @Column(name = "party_acronym")
  private String partyAcronym;

  private long votes;

  private int rank;

  private boolean elected;

  @Column(name = "politician_account_id")
  private UUID politicianAccountId;

  protected ElectionResultJpaEntity() {}

  public ElectionResultJpaEntity(
      UUID id,
      UUID electionId,
      String office,
      String externalId,
      String candidateName,
      String partyAcronym,
      long votes,
      int rank,
      boolean elected,
      UUID politicianAccountId) {
    this.id = id;
    this.electionId = electionId;
    this.office = office;
    this.externalId = externalId;
    this.candidateName = candidateName;
    this.partyAcronym = partyAcronym;
    this.votes = votes;
    this.rank = rank;
    this.elected = elected;
    this.politicianAccountId = politicianAccountId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getElectionId() {
    return electionId;
  }

  public String getOffice() {
    return office;
  }

  public String getExternalId() {
    return externalId;
  }

  public String getCandidateName() {
    return candidateName;
  }

  public String getPartyAcronym() {
    return partyAcronym;
  }

  public long getVotes() {
    return votes;
  }

  public int getRank() {
    return rank;
  }

  public boolean isElected() {
    return elected;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }
}
