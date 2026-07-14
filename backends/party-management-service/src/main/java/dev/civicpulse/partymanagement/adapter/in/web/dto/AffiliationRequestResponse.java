package dev.civicpulse.partymanagement.adapter.in.web.dto;

import dev.civicpulse.partymanagement.domain.model.AffiliationRequest;
import java.time.Instant;
import java.util.UUID;

public record AffiliationRequestResponse(
    UUID id, UUID partyId, UUID citizenAccountId, String city, String status, Instant requestedAt, Instant decidedAt) {

  public static AffiliationRequestResponse from(AffiliationRequest request) {
    return new AffiliationRequestResponse(
        request.id(),
        request.partyId(),
        request.citizenAccountId(),
        request.city().orElse(null),
        request.status().code(),
        request.requestedAt(),
        request.decidedAt().orElse(null));
  }
}
