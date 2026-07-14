package dev.civicpulse.fundraising.adapter.in.web.dto;

import dev.civicpulse.fundraising.domain.model.Contribution;
import java.time.Instant;
import java.util.UUID;

public record ContributionResponse(UUID id, UUID fundraiserId, UUID supporterAccountId, long amountCents, UUID paymentIntentId, Instant createdAt) {

  public static ContributionResponse from(Contribution contribution) {
    return new ContributionResponse(
        contribution.id(), contribution.fundraiserId(), contribution.supporterAccountId(), contribution.amountCents(),
        contribution.paymentIntentId(), contribution.createdAt());
  }
}
