package dev.civicpulse.identity.adapter.out.messaging;

import dev.civicpulse.identity.application.port.out.EventPublisher;
import dev.civicpulse.identity.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** See the Communication Patterns section of docs/architecture/system-architecture.html: this
 * is the default, asynchronous channel for cross-service consistency. Topic names come
 * straight from {@link DomainEvent#topic()}, matching infra/kafka-init/topics.txt exactly. */
@Component
public class KafkaEventPublisherAdapter implements EventPublisher {

  private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisherAdapter.class);

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public KafkaEventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void publish(DomainEvent event) {
    kafkaTemplate
        .send(event.topic(), event)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error("Failed to publish {} to topic {}", event.getClass().getSimpleName(), event.topic(), ex);
              } else {
                log.debug("Published {} to topic {}", event.getClass().getSimpleName(), event.topic());
              }
            });
  }
}
