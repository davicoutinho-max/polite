package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.event.DomainEvent;

public interface LegislativeEventPublisher {

  void publish(DomainEvent event);
}
