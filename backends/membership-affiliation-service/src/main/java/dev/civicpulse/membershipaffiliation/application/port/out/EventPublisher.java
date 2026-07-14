package dev.civicpulse.membershipaffiliation.application.port.out;

import dev.civicpulse.membershipaffiliation.domain.event.DomainEvent;

public interface EventPublisher {

  void publish(DomainEvent event);
}
