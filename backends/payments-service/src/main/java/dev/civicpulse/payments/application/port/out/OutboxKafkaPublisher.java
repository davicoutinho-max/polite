package dev.civicpulse.payments.application.port.out;

import dev.civicpulse.payments.domain.event.DomainEvent;

/** Used only by application.OutboxRelayService — never called from within a request
 * transaction (that's the entire point of the outbox pattern: the DB commit must never be
 * gated on a Kafka round trip). */
public interface OutboxKafkaPublisher {

  /** Blocks until the broker acknowledges — the relay must know publish succeeded before
   * marking the outbox row as published. */
  void publishAndWait(DomainEvent event);
}
