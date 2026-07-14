package dev.civicpulse.participation.domain.event;

import java.time.Instant;

public sealed interface DomainEvent permits PetitionSigned, ConsultationStanceSet, SurveyVoteCast {

  String topic();

  Instant occurredAt();
}
