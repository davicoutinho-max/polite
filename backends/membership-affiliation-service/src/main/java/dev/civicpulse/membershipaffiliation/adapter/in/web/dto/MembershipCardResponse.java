package dev.civicpulse.membershipaffiliation.adapter.in.web.dto;

import dev.civicpulse.membershipaffiliation.domain.model.MembershipCard;
import java.time.Instant;
import java.util.UUID;

public record MembershipCardResponse(UUID affiliationId, String memberNumber, String qrPayload, Instant issuedAt) {

  public static MembershipCardResponse from(MembershipCard card) {
    return new MembershipCardResponse(card.affiliationId(), card.memberNumber(), card.qrPayload(), card.issuedAt());
  }
}
