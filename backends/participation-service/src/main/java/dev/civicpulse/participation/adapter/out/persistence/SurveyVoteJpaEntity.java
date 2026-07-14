package dev.civicpulse.participation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "survey_votes")
@IdClass(SurveyVoteId.class)
public class SurveyVoteJpaEntity {

  @Id
  @Column(name = "survey_id")
  private UUID surveyId;

  @Id
  @Column(name = "citizen_account_id")
  private UUID citizenAccountId;

  @Column(name = "option_id", nullable = false)
  private UUID optionId;

  @Column(name = "voted_at", nullable = false)
  private Instant votedAt;

  protected SurveyVoteJpaEntity() {}

  public SurveyVoteJpaEntity(UUID surveyId, UUID citizenAccountId, UUID optionId, Instant votedAt) {
    this.surveyId = surveyId;
    this.citizenAccountId = citizenAccountId;
    this.optionId = optionId;
    this.votedAt = votedAt;
  }

  public UUID getSurveyId() {
    return surveyId;
  }

  public UUID getCitizenAccountId() {
    return citizenAccountId;
  }

  public UUID getOptionId() {
    return optionId;
  }

  public Instant getVotedAt() {
    return votedAt;
  }
}
