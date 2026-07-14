package dev.civicpulse.privacycompliance.adapter.in.messaging;

import dev.civicpulse.privacycompliance.adapter.in.messaging.dto.ErasureCompletedMessage;
import dev.civicpulse.privacycompliance.application.port.in.ManageAccountDeletionUseCase;
import dev.civicpulse.privacycompliance.domain.exception.AccountDeletionRequestNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class ErasureProjectionListener {

  private static final Logger log = LoggerFactory.getLogger(ErasureProjectionListener.class);

  private final ManageAccountDeletionUseCase manageAccountDeletionUseCase;

  ErasureProjectionListener(ManageAccountDeletionUseCase manageAccountDeletionUseCase) {
    this.manageAccountDeletionUseCase = manageAccountDeletionUseCase;
  }

  @KafkaListener(topics = "erasure-completed", groupId = "privacy-compliance-service")
  void onErasureCompleted(ErasureCompletedMessage message) {
    try {
      manageAccountDeletionUseCase.onErasureReported(message.deletionRequestId(), message.serviceName(), message.recordCount());
    } catch (AccountDeletionRequestNotFoundException e) {
      log.warn("Ignoring erasure-completed for unknown deletionRequestId {} (service {})", message.deletionRequestId(), message.serviceName());
    }
  }
}
