package dev.civicpulse.legislative.adapter.out.messaging;

import dev.civicpulse.legislative.application.port.out.LegislativeEventPublisher;
import dev.civicpulse.legislative.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class KafkaEventPublisherAdapter implements LegislativeEventPublisher {

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
                log.error("Failed to publish {} to topic {}", event.getClass().getSimpleName(), event.topic(), ex);
              } else {
                log.debug("Published {} to topic {}", event.getClass().getSimpleName(), event.topic());
              }
            });
  }
}
