package dev.civicpulse.partymanagement.application.port.out;

import dev.civicpulse.partymanagement.domain.event.DomainEvent;

public interface EventPublisher {

  void publish(DomainEvent event);
}
