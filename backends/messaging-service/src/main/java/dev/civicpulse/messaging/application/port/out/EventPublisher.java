package dev.civicpulse.messaging.application.port.out;

import dev.civicpulse.messaging.domain.event.DomainEvent;

public interface EventPublisher {

  void publish(DomainEvent event);
}
