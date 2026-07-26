package dev.civicpulse.platformconfig.application.port.in;

import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;

/** Government-data-sync path (see government-sync-service) — upserts by {@code acronym} rather
 * than requiring a caller-supplied id, since real parties are a small (~30), stable set: a first
 * call creates the registry entry, a repeat call updates name/logo if they changed and otherwise
 * returns the existing entry untouched. */
public interface SyncPartyUseCase {

  PartyRegistryEntry syncParty(SyncPartyCommand command);

  record SyncPartyCommand(
      String name,
      String acronym,
      int number,
      String logoUrl,
      String documentType,
      String rawDocumentNumber,
      String externalSource,
      String externalId) {}
}
