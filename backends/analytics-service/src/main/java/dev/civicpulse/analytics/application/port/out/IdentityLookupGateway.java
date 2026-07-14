package dev.civicpulse.analytics.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** Resolves an account's type (citizen/politician/party/admin) — the real, always-populated
 * dimension this service reports engagement by instead of a fabricated geography, see
 * schema.sql's header note. */
public interface IdentityLookupGateway {

  Optional<String> lookupAccountType(UUID accountId);
}
