package dev.civicpulse.participation.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SurveyVote {

  private final UUID surveyId;
  private final UUID citizenAccountId;
  private final UUID optionId;
  private final Instant votedAt;

  private SurveyVote(UUID surveyId, UUID citizenAccountId, UUID optionId, Instant votedAt) {
    this.surveyId = Objects.requireNonNull(surveyId);
    this.citizenAccountId = Objects.requireNonNull(citizenAccountId);
    this.optionId = Objects.requireNonNull(optionId);
    this.votedAt = Objects.requireNonNull(votedAt);
  }

  public static SurveyVote cast(UUID surveyId, UUID citizenAccountId, UUID optionId, Instant now) {
    return new SurveyVote(surveyId, citizenAccountId, optionId, now);
  }

  public static SurveyVote reconstitute(UUID surveyId, UUID citizenAccountId, UUID optionId, Instant votedAt) {
    return new SurveyVote(surveyId, citizenAccountId, optionId, votedAt);
  }

  public UUID surveyId() {
    return surveyId;
  }

  public UUID citizenAccountId() {
    return citizenAccountId;
  }

  public UUID optionId() {
    return optionId;
  }

  public Instant votedAt() {
    return votedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SurveyVote other)) return false;
    return surveyId.equals(other.surveyId) && citizenAccountId.equals(other.citizenAccountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(surveyId, citizenAccountId);
  }
}
