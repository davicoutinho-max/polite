package dev.civicpulse.privacycompliance.application.port.out;

import dev.civicpulse.privacycompliance.domain.event.DomainEvent;

public interface EventPublisher {

  void publish(DomainEvent event);
}
