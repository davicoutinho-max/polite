package dev.civicpulse.fundraising.adapter.in.web.dto;

import dev.civicpulse.fundraising.domain.model.Fundraiser;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FundraiserResponse(
    UUID id,
    UUID organizerAccountId,
    String title,
    String description,
    String category,
    long goalCents,
    long raisedCents,
    int supportersCount,
    LocalDate deadline,
    boolean ledgerPublic,
    Instant createdAt) {

  public static FundraiserResponse from(Fundraiser fundraiser) {
    return new FundraiserResponse(
        fundraiser.id(),
        fundraiser.organizerAccountId(),
        fundraiser.title(),
        fundraiser.description().orElse(null),
        fundraiser.category().code(),
        fundraiser.goalCents(),
        fundraiser.raisedCents(),
        fundraiser.supportersCount(),
        fundraiser.deadline().orElse(null),
        fundraiser.ledgerPublic(),
        fundraiser.createdAt());
  }
}
