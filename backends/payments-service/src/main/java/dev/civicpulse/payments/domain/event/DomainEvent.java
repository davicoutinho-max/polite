package dev.civicpulse.payments.domain.event;

import java.time.Instant;

/** Unlike every other service, these events are never published directly inside the request
 * transaction — see application.OutboxRelayService. They're serialized into {@code
 * outbox_events.payload} in the same DB transaction as the {@code payment_intents} status
 * change, and a separate scheduled relay deserializes and publishes them to Kafka. This is the
 * transactional outbox pattern (see docs/db/payments-service/schema.sql's comment on {@code
 * outbox_events}) — it exists specifically so a Kafka publish failure can never desync from a
 * committed charge. */
public sealed interface DomainEvent permits PaymentAuthorized, PaymentCaptured, PaymentFailed, PaymentRefunded {

  String topic();

  Instant occurredAt();
}
