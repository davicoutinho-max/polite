package dev.civicpulse.membershipaffiliation.domain.event;

import java.time.Instant;

public sealed interface DomainEvent
    permits AffiliationRequested, AffiliationUnderReview, AffiliationConfirmed, MembershipCardIssued, MembershipFeeGenerated, MembershipFeeOverdue {

  String topic();

  Instant occurredAt();
}
