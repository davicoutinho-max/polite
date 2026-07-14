package dev.civicpulse.participation.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class SurveyOption {

  private final UUID id;
  private final UUID surveyId;
  private final String label;
  private int votesCount;

  private SurveyOption(UUID id, UUID surveyId, String label, int votesCount) {
    this.id = Objects.requireNonNull(id);
    this.surveyId = Objects.requireNonNull(surveyId);
    this.label = requireNonBlank(label);
    this.votesCount = votesCount;
  }

  public static SurveyOption create(UUID id, UUID surveyId, String label) {
    return new SurveyOption(id, surveyId, label, 0);
  }

  public static SurveyOption reconstitute(UUID id, UUID surveyId, String label, int votesCount) {
    return new SurveyOption(id, surveyId, label, votesCount);
  }

  public void incrementVotes() {
    votesCount++;
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("label must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID surveyId() {
    return surveyId;
  }

  public String label() {
    return label;
  }

  public int votesCount() {
    return votesCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SurveyOption other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
