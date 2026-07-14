package dev.civicpulse.participation.domain.event;

import java.time.Instant;
import java.util.UUID;

public record SurveyVoteCast(UUID surveyId, UUID citizenAccountId, UUID optionId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "survey-vote-cast";
  }
}
