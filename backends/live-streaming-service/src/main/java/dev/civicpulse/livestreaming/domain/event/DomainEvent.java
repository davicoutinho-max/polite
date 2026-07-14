package dev.civicpulse.livestreaming.domain.event;

import java.time.Instant;

public sealed interface DomainEvent permits LiveSessionScheduled, LiveSessionStarted, LiveSessionEnded {

  String topic();

  Instant occurredAt();
}
