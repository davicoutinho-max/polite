package dev.civicpulse.membershipaffiliation.adapter.in.messaging;

import dev.civicpulse.membershipaffiliation.adapter.in.messaging.dto.AffiliationRequestApprovedMessage;
import dev.civicpulse.membershipaffiliation.adapter.in.messaging.dto.AffiliationRequestRejectedMessage;
import dev.civicpulse.membershipaffiliation.adapter.in.messaging.dto.PaymentCapturedMessage;
import dev.civicpulse.membershipaffiliation.application.port.in.ManageAffiliationUseCase;
import dev.civicpulse.membershipaffiliation.application.port.in.ManageMembershipFeeUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class MembershipAffiliationProjectionListener {

  private final ManageAffiliationUseCase manageAffiliationUseCase;
  private final ManageMembershipFeeUseCase manageMembershipFeeUseCase;

  MembershipAffiliationProjectionListener(
      ManageAffiliationUseCase manageAffiliationUseCase, ManageMembershipFeeUseCase manageMembershipFeeUseCase) {
    this.manageAffiliationUseCase = manageAffiliationUseCase;
    this.manageMembershipFeeUseCase = manageMembershipFeeUseCase;
  }

  @KafkaListener(topics = "affiliation-request-approved", groupId = "membership-affiliation-service")
  void onAffiliationRequestApproved(AffiliationRequestApprovedMessage message) {
    manageAffiliationUseCase.onAffiliationRequestApproved(message.requestId());
  }

  @KafkaListener(topics = "affiliation-request-rejected", groupId = "membership-affiliation-service")
  void onAffiliationRequestRejected(AffiliationRequestRejectedMessage message) {
    manageAffiliationUseCase.onAffiliationRequestRejected(message.requestId());
  }

  @KafkaListener(topics = "payment-captured", groupId = "membership-affiliation-service")
  void onPaymentCaptured(PaymentCapturedMessage message) {
    manageMembershipFeeUseCase.onPaymentCaptured(message.referenceId(), message.paymentIntentId());
  }
}
