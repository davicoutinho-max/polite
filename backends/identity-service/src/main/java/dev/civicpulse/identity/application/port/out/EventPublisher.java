package dev.civicpulse.identity.application.port.out;

import dev.civicpulse.identity.domain.event.DomainEvent;

/** Publishes to Kafka (topic = {@link DomainEvent#topic()}). See the Communication Patterns
 * section of docs/architecture/system-architecture.html: this is the default channel for
 * cross-service consistency — never a direct call into another service's database. */
public interface EventPublisher {

  void publish(DomainEvent event);
}
