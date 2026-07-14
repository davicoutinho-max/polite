package dev.civicpulse.directory.adapter.out.messaging;

import dev.civicpulse.directory.application.port.out.EventPublisher;
import dev.civicpulse.directory.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class KafkaEventPublisherAdapter implements EventPublisher {

  private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisherAdapter.class);

  private final KafkaTemplate<String, Object> kafkaTemplate;

  KafkaEventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void publish(DomainEvent event) {
    kafkaTemplate
        .send(event.topic(), event)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                log.error("Failed to publish {} to topic {}", event, event.topic(), ex);
              } else {
                log.debug("Published {} to topic {}", event, event.topic());
              }
            });
  }
}
