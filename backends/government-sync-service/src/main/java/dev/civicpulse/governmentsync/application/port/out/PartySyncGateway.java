package dev.civicpulse.governmentsync.application.port.out;

import java.util.UUID;

/** Calls platform-configuration-service's internal {@code POST /parties/sync} — see that
 * service's {@code SyncPartyUseCase} for the upsert-by-acronym contract this mirrors. */
public interface PartySyncGateway {

  UUID syncParty(SyncPartyCommand command);

  record SyncPartyCommand(
      String name,
      String acronym,
      int number,
      String logoUrl,
      String documentNumber,
      String externalSource,
      String externalId) {}
}
