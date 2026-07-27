package dev.civicpulse.elections.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "personal_votes")
public class PersonalVoteJpaEntity {

  @Id private UUID id;

  @Column(name = "citizen_account_id")
  private UUID citizenAccountId;

  @Column(name = "election_id")
  private UUID electionId;

  private String office;

  @Column(name = "candidate_name")
  private String candidateName;

  @Column(name = "candidate_party_acronym")
  private String candidatePartyAcronym;

  @Column(name = "politician_account_id")
  private UUID politicianAccountId;

  @Column(name = "cast_at")
  private Instant castAt;

  protected PersonalVoteJpaEntity() {}

  public PersonalVoteJpaEntity(
      UUID id,
      UUID citizenAccountId,
      UUID electionId,
      String office,
      String candidateName,
      String candidatePartyAcronym,
      UUID politicianAccountId,
      Instant castAt) {
    this.id = id;
    this.citizenAccountId = citizenAccountId;
    this.electionId = electionId;
    this.office = office;
    this.candidateName = candidateName;
    this.candidatePartyAcronym = candidatePartyAcronym;
    this.politicianAccountId = politicianAccountId;
    this.castAt = castAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getCitizenAccountId() {
    return citizenAccountId;
  }

  public UUID getElectionId() {
    return electionId;
  }

  public String getOffice() {
    return office;
  }

  public String getCandidateName() {
    return candidateName;
  }

  public String getCandidatePartyAcronym() {
    return candidatePartyAcronym;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public Instant getCastAt() {
    return castAt;
  }
}
