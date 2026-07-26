package dev.civicpulse.partymanagement.application.port.in;

import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.util.UUID;

/** Government-data-sync path (see government-sync-service) — provisions/updates the politician's
 * account and keeps their party link in sync, idempotently. Unlike {@link RegisterPoliticianUseCase},
 * safe to call repeatedly with the same external record: a first call creates, a repeat call with
 * unchanged data is a no-op, and a repeat call naming a different party unlinks the old
 * representative row and links a new one (a real, common event — Brazilian politicians switch
 * parties, "troca de partido"). */
public interface SyncPoliticianUseCase {

  PartyRepresentative syncPolitician(UUID partyId, SyncPoliticianCommand command);

  record SyncPoliticianCommand(
      String name,
      String handle,
      String email,
      String avatarUrl,
      String documentType,
      String rawDocumentNumber,
      String externalSource,
      String externalId,
      String roleTitle,
      String state,
      String govLevel) {}
}
