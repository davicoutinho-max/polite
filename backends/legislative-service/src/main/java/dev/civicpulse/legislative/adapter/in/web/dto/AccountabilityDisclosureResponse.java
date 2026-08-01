package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.AccountabilityDisclosure;
import java.time.Instant;
import java.util.UUID;

public record AccountabilityDisclosureResponse(
    UUID id,
    UUID politicianAccountId,
    String category,
    long declaredAmountCents,
    String documentUrl,
    String status,
    Long extractedAmountCents,
    String aiFeedback,
    Instant submittedAt) {

  public static AccountabilityDisclosureResponse from(AccountabilityDisclosure disclosure) {
    return new AccountabilityDisclosureResponse(
        disclosure.id(),
        disclosure.politicianAccountId(),
        disclosure.category().code(),
        disclosure.declaredAmountCents(),
        disclosure.documentUrl(),
        disclosure.status().code(),
        disclosure.extractedAmountCents().orElse(null),
        disclosure.aiFeedback(),
        disclosure.submittedAt());
  }
}
