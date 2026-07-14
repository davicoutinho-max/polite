package dev.civicpulse.fundraising.application.port.out;

import dev.civicpulse.fundraising.domain.event.DomainEvent;

public interface EventPublisher {

  void publish(DomainEvent event);
}
