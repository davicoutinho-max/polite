package dev.civicpulse.platformconfig.adapter.in.messaging;

import dev.civicpulse.platformconfig.adapter.in.messaging.dto.PoliticianRegisteredMessage;
import dev.civicpulse.platformconfig.application.port.in.ManagePoliticianAssignmentUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class PlatformConfigProjectionListener {

  private final ManagePoliticianAssignmentUseCase managePoliticianAssignmentUseCase;

  PlatformConfigProjectionListener(ManagePoliticianAssignmentUseCase managePoliticianAssignmentUseCase) {
    this.managePoliticianAssignmentUseCase = managePoliticianAssignmentUseCase;
  }

  @KafkaListener(topics = "politician-registered", groupId = "platform-configuration-service")
  void onPoliticianRegistered(PoliticianRegisteredMessage message) {
    managePoliticianAssignmentUseCase.onPoliticianRegistered(message.politicianAccountId(), message.partyId());
  }
}
