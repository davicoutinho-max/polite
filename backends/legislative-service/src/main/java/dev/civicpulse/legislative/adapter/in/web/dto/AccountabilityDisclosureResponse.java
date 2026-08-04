package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.AccountabilityDisclosure;
import java.time.Instant;
import java.util.UUID;

public record AccountabilityDisclosureResponse(
    UUID id,
    UUID politicianAccountId,
    String category,
    int periodMonth,
    int periodYear,
    long declaredAmountCents,
    String documentUrl,
    String status,
    Long extractedAmountCents,
    String aiFeedback,
    String notes,
    Instant submittedAt) {

  public static AccountabilityDisclosureResponse from(AccountabilityDisclosure disclosure) {
    return new AccountabilityDisclosureResponse(
        disclosure.id(),
        disclosure.politicianAccountId(),
        disclosure.category(),
        disclosure.periodMonth(),
        disclosure.periodYear(),
        disclosure.declaredAmountCents(),
        disclosure.documentUrl(),
        disclosure.status().code(),
        disclosure.extractedAmountCents().orElse(null),
        disclosure.aiFeedback(),
        disclosure.notes().orElse(null),
        disclosure.submittedAt());
  }
}
