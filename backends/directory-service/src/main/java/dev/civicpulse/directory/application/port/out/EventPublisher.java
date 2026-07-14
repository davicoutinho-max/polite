package dev.civicpulse.directory.application.port.out;

import dev.civicpulse.directory.domain.event.DomainEvent;

public interface EventPublisher {

  void publish(DomainEvent event);
}
