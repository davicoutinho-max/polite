package dev.civicpulse.partymanagement.adapter.in.messaging;

import dev.civicpulse.partymanagement.adapter.in.messaging.dto.AffiliationRequestedMessage;
import dev.civicpulse.partymanagement.adapter.in.messaging.dto.PartyRegisteredMessage;
import dev.civicpulse.partymanagement.application.port.in.ManagePartyProfileUseCase;
import dev.civicpulse.partymanagement.application.port.in.ReviewAffiliationRequestUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Translates the events listed in docs/db/party-management-service/schema.sql's "Domain
 * events consumed" section into use-case calls. */
@Component
class PartyManagementProjectionListener {

  private final ManagePartyProfileUseCase managePartyProfileUseCase;
  private final ReviewAffiliationRequestUseCase reviewAffiliationRequestUseCase;

  PartyManagementProjectionListener(
      ManagePartyProfileUseCase managePartyProfileUseCase, ReviewAffiliationRequestUseCase reviewAffiliationRequestUseCase) {
    this.managePartyProfileUseCase = managePartyProfileUseCase;
    this.reviewAffiliationRequestUseCase = reviewAffiliationRequestUseCase;
  }

  @KafkaListener(topics = "party-registered", groupId = "party-management-service")
  void onPartyRegistered(PartyRegisteredMessage message) {
    managePartyProfileUseCase.onPartyRegistered(message.partyId());
  }

  @KafkaListener(topics = "affiliation-requested", groupId = "party-management-service")
  void onAffiliationRequested(AffiliationRequestedMessage message) {
    reviewAffiliationRequestUseCase.onAffiliationRequested(
        message.affiliationId(), message.partyId(), message.citizenAccountId(), message.city(), message.occurredAt());
  }
}
