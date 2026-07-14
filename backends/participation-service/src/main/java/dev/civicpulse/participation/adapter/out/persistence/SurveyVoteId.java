package dev.civicpulse.participation.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class SurveyVoteId implements Serializable {

  private UUID surveyId;
  private UUID citizenAccountId;

  protected SurveyVoteId() {}

  public SurveyVoteId(UUID surveyId, UUID citizenAccountId) {
    this.surveyId = surveyId;
    this.citizenAccountId = citizenAccountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SurveyVoteId other)) return false;
    return Objects.equals(surveyId, other.surveyId) && Objects.equals(citizenAccountId, other.citizenAccountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(surveyId, citizenAccountId);
  }
}
