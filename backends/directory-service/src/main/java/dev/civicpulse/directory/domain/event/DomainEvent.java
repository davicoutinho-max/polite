package dev.civicpulse.directory.domain.event;

import java.time.Instant;

/** Sealed root for events this service publishes. Events this service *consumes* from other
 * services are not modeled here — they're plain DTOs owned by the inbound Kafka adapter (see
 * adapter.in.messaging.dto), since this service only needs a subset of their fields and must
 * not couple to another service's event class. */
public sealed interface DomainEvent permits FollowCreated, FollowRemoved {

  String topic();

  Instant occurredAt();
}
