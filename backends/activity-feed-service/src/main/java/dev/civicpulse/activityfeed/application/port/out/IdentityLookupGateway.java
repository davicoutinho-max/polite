package dev.civicpulse.activityfeed.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** Resolves an account's display name — identity-service is the only service that has a name
 * for every account type (citizen/politician/party/admin), unlike directory-service which only
 * knows about politicians and parties. */
public interface IdentityLookupGateway {

  Optional<String> lookupDisplayName(UUID accountId);
}
