package dev.civicpulse.identity.domain.event;

import java.time.Instant;

/** Marker for anything published via the outbound {@code EventPublisher} port. {@code topic()}
 * is the kebab-case Kafka topic name from infra/kafka-init/topics.txt. */
public sealed interface DomainEvent
    permits AccountRegistered, SessionIssued, SessionRevoked, AccountVerified {

  String topic();

  Instant occurredAt();
}
