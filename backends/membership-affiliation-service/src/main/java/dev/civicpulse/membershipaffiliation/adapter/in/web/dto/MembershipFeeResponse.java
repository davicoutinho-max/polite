package dev.civicpulse.membershipaffiliation.adapter.in.web.dto;

import dev.civicpulse.membershipaffiliation.domain.model.MembershipFee;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MembershipFeeResponse(
    UUID id,
    UUID affiliationId,
    String referencePeriod,
    long amountCents,
    LocalDate dueDate,
    String status,
    Instant paidAt,
    UUID paymentIntentId) {

  public static MembershipFeeResponse from(MembershipFee fee) {
    return new MembershipFeeResponse(
        fee.id(),
        fee.affiliationId(),
        fee.referencePeriod(),
        fee.amountCents(),
        fee.dueDate(),
        fee.status().code(),
        fee.paidAt().orElse(null),
        fee.paymentIntentId().orElse(null));
  }
}
