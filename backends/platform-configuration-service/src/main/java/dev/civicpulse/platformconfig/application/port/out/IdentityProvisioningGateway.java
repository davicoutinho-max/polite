package dev.civicpulse.platformconfig.application.port.out;

import java.util.UUID;

/** Anti-corruption-layer boundary: registering a brand-new party means creating its
 * authenticatable identity, which only Identity Service owns (see
 * docs/architecture/system-architecture.html — this service never touches Identity's
 * database). The returned account id becomes the party's own id everywhere else in the
 * platform (party-management-service's PartyProfile, directory-service's public listing) —
 * mirroring party-management-service's identical pattern for provisioning a politician. */
public interface IdentityProvisioningGateway {

  ProvisionedAccount provisionPartyAccount(
      String name, String handle, String email, String rawPassword, String documentType, String rawDocumentNumber);

  /** Government-data-sync path (see government-sync-service) — the party never signed up, so
   * there is no password; idempotent by {@code (externalSource, externalId)} on Identity's side. */
  ProvisionedAccount provisionSyncedPartyAccount(
      String name,
      String handle,
      String email,
      String avatarUrl,
      String documentType,
      String rawDocumentNumber,
      String externalSource,
      String externalId);

  record ProvisionedAccount(UUID accountId, String name, String handle) {}
}
