package dev.civicpulse.payments.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.civicpulse.payments.application.port.out.OutboxEventRepository;
import dev.civicpulse.payments.application.port.out.OutboxKafkaPublisher;
import dev.civicpulse.payments.domain.event.DomainEvent;
import dev.civicpulse.payments.domain.event.PaymentAuthorized;
import dev.civicpulse.payments.domain.event.PaymentCaptured;
import dev.civicpulse.payments.domain.event.PaymentFailed;
import dev.civicpulse.payments.domain.event.PaymentRefunded;
import dev.civicpulse.payments.domain.model.OutboxEvent;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The relay half of the transactional outbox pattern (see docs/db/payments-service/schema.sql
 * and domain.event.DomainEvent's javadoc): polls {@code outbox_events} for unpublished rows,
 * replays them to Kafka, and marks them published — the only place in this service that
 * actually talks to Kafka. */
@Service
@EnableScheduling
public class OutboxRelayService {

  private static final Logger log = LoggerFactory.getLogger(OutboxRelayService.class);
  private static final int BATCH_SIZE = 50;

  private final OutboxEventRepository outboxEventRepository;
  private final OutboxKafkaPublisher outboxKafkaPublisher;
  private final ObjectMapper objectMapper;
  private final Clock clock;

  public OutboxRelayService(
      OutboxEventRepository outboxEventRepository, OutboxKafkaPublisher outboxKafkaPublisher, ObjectMapper objectMapper, Clock clock) {
    this.outboxEventRepository = outboxEventRepository;
    this.outboxKafkaPublisher = outboxKafkaPublisher;
    this.objectMapper = objectMapper;
    this.clock = clock;
  }

  @Scheduled(fixedDelay = 2000)
  @Transactional
  public void relayUnpublishedEvents() {
    for (OutboxEvent row : outboxEventRepository.findUnpublished(BATCH_SIZE)) {
      try {
        DomainEvent event = deserialize(row.eventType(), row.payload());
        outboxKafkaPublisher.publishAndWait(event);
        row.markPublished(clock.instant());
        outboxEventRepository.save(row);
      } catch (Exception e) {
        // Left unpublished — picked up again on the next poll. No dead-letter queue in this
        // demo; a stuck row would need manual inspection (see idx_outbox_unpublished).
        log.error("Failed to relay outbox event {} ({})", row.id(), row.eventType(), e);
      }
    }
  }

  private DomainEvent deserialize(String eventType, String payload) throws JsonProcessingException {
    return switch (eventType) {
      case "PaymentAuthorized" -> objectMapper.readValue(payload, PaymentAuthorized.class);
      case "PaymentCaptured" -> objectMapper.readValue(payload, PaymentCaptured.class);
      case "PaymentFailed" -> objectMapper.readValue(payload, PaymentFailed.class);
      case "PaymentRefunded" -> objectMapper.readValue(payload, PaymentRefunded.class);
      default -> throw new IllegalStateException("Unknown outbox event_type: " + eventType);
    };
  }
}
