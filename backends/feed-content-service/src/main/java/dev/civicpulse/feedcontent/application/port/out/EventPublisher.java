package dev.civicpulse.feedcontent.application.port.out;

import dev.civicpulse.feedcontent.domain.event.DomainEvent;

public interface EventPublisher {

  void publish(DomainEvent event);
}
