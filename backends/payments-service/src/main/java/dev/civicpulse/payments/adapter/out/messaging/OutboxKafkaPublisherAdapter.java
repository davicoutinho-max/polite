package dev.civicpulse.payments.adapter.out.messaging;

import dev.civicpulse.payments.application.port.out.OutboxKafkaPublisher;
import dev.civicpulse.payments.domain.event.DomainEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class OutboxKafkaPublisherAdapter implements OutboxKafkaPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  OutboxKafkaPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void publishAndWait(DomainEvent event) {
    try {
      kafkaTemplate.send(event.topic(), event).get(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted publishing " + event.getClass().getSimpleName(), e);
    } catch (ExecutionException | TimeoutException e) {
      throw new IllegalStateException("Failed to publish " + event.getClass().getSimpleName(), e);
    }
  }
}
