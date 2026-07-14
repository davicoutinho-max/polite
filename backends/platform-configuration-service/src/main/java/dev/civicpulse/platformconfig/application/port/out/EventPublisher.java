package dev.civicpulse.platformconfig.application.port.out;

import dev.civicpulse.platformconfig.domain.event.DomainEvent;

public interface EventPublisher {

  void publish(DomainEvent event);
}
