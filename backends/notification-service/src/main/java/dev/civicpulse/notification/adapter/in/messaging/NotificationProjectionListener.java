package dev.civicpulse.notification.adapter.in.messaging;

import dev.civicpulse.notification.adapter.in.messaging.dto.AffiliationConfirmedMessage;
import dev.civicpulse.notification.adapter.in.messaging.dto.ContributionReceivedMessage;
import dev.civicpulse.notification.adapter.in.messaging.dto.FundraiserGoalReachedMessage;
import dev.civicpulse.notification.adapter.in.messaging.dto.MembershipFeeGeneratedMessage;
import dev.civicpulse.notification.application.NotificationProjectionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Wires the subset of "every other note-worthy event in the system" (see schema.sql's
 * open-ended consumed-events list) where the recipient is directly on the event or resolvable via
 * one real synchronous lookup — see NotificationServiceApplication's scope note for what's
 * deliberately not wired in this pass and why. */
@Component
class NotificationProjectionListener {

  private final NotificationProjectionService projectionService;

  NotificationProjectionListener(NotificationProjectionService projectionService) {
    this.projectionService = projectionService;
  }

  @KafkaListener(topics = "affiliation-confirmed", groupId = "notification-service")
  void onAffiliationConfirmed(AffiliationConfirmedMessage message) {
    projectionService.onAffiliationConfirmed(message.affiliationId(), message.citizenAccountId(), message.partyId());
  }

  @KafkaListener(topics = "membership-fee-generated", groupId = "notification-service")
  void onMembershipFeeGenerated(MembershipFeeGeneratedMessage message) {
    projectionService.onMembershipFeeGenerated(message.feeId(), message.affiliationId(), message.amountCents());
  }

  @KafkaListener(topics = "contribution-received", groupId = "notification-service")
  void onContributionReceived(ContributionReceivedMessage message) {
    projectionService.onContributionReceived(message.fundraiserId(), message.contributionId(), message.supporterAccountId(), message.amountCents());
  }

  @KafkaListener(topics = "fundraiser-goal-reached", groupId = "notification-service")
  void onFundraiserGoalReached(FundraiserGoalReachedMessage message) {
    projectionService.onFundraiserGoalReached(message.fundraiserId(), message.raisedCents());
  }
}
