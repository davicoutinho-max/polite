package dev.civicpulse.elections.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** Anti-corruption-layer port to directory-service — a real synchronous REST call (see
 * party-management-service's IdentityProvisioningGateway for the established pattern). Candidate
 * profile details are resolved here at query time rather than replicated locally, per
 * {@code election_candidacies.politician_account_id}'s comment in schema.sql. */
public interface PoliticianDirectoryGateway {

  /** Empty when directory-service has no record of this politician (e.g. deleted) — a real
   * lookup failure (unreachable, 5xx) throws {@code PoliticianLookupException} instead. */
  Optional<PoliticianSummary> lookup(UUID politicianAccountId);

  record PoliticianSummary(
      UUID accountId, String name, String handle, String avatarUrl, boolean verified, String office, String partyAcronym) {}
}
