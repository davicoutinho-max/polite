package dev.civicpulse.participation.application.port.out;

import dev.civicpulse.participation.domain.event.DomainEvent;

public interface EventPublisher {

  void publish(DomainEvent event);
}
