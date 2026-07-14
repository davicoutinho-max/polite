package dev.civicpulse.activityfeed.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** {@code FundraiserGoalReached} only carries the fundraiser's id, not its organizer or title —
 * this gateway resolves both so the achievement can be attributed and described on the
 * organizer's timeline. */
public interface FundraiserLookupGateway {

  Optional<FundraiserSummary> lookupFundraiser(UUID fundraiserId);

  record FundraiserSummary(UUID organizerAccountId, String title) {}
}
