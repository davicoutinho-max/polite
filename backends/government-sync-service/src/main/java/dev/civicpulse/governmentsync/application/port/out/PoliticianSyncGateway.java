package dev.civicpulse.governmentsync.application.port.out;

import java.util.UUID;

/** Calls party-management-service's internal {@code POST /politicians/sync} — see that service's
 * {@code SyncPoliticianUseCase} for the idempotent upsert/party-switch contract this mirrors. */
public interface PoliticianSyncGateway {

  /** Returns the politician's account id — needed so callers can enrich the profile afterward
   * (see LegislativeDossierGateway) without a separate lookup. */
  UUID syncPolitician(UUID partyId, SyncPoliticianCommand command);

  record SyncPoliticianCommand(
      String name,
      String handle,
      String email,
      String avatarUrl,
      String documentNumber,
      String externalSource,
      String externalId,
      String roleTitle,
      String state,
      String govLevel) {}
}
