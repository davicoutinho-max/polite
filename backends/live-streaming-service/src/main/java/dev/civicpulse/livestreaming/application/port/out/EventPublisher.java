package dev.civicpulse.livestreaming.application.port.out;

import dev.civicpulse.livestreaming.domain.event.DomainEvent;

public interface EventPublisher {

  void publish(DomainEvent event);
}
