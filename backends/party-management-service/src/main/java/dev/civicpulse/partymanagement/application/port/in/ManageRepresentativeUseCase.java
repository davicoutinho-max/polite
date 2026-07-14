package dev.civicpulse.partymanagement.application.port.in;

import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.util.List;
import java.util.UUID;

public interface ManageRepresentativeUseCase {

  /** Links an existing politician account to a party — the Platform Admin reassignment path
   * (flow 03), as opposed to {@link RegisterPoliticianUseCase} which creates a brand-new
   * account. */
  PartyRepresentative linkExisting(UUID partyId, UUID politicianAccountId, String roleTitle);

  void unlink(UUID partyId, UUID politicianAccountId);

  List<PartyRepresentative> listByParty(UUID partyId);
}
