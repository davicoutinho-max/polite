package dev.civicpulse.participation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "survey_options")
public class SurveyOptionJpaEntity {

  @Id private UUID id;

  @Column(name = "survey_id", nullable = false)
  private UUID surveyId;

  @Column(nullable = false)
  private String label;

  @Column(name = "votes_count", nullable = false)
  private int votesCount;

  protected SurveyOptionJpaEntity() {}

  public SurveyOptionJpaEntity(UUID id, UUID surveyId, String label, int votesCount) {
    this.id = id;
    this.surveyId = surveyId;
    this.label = label;
    this.votesCount = votesCount;
  }

  public UUID getId() {
    return id;
  }

  public UUID getSurveyId() {
    return surveyId;
  }

  public String getLabel() {
    return label;
  }

  public int getVotesCount() {
    return votesCount;
  }
}
