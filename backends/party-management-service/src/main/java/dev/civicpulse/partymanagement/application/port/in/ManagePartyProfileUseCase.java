package dev.civicpulse.partymanagement.application.port.in;

import dev.civicpulse.partymanagement.domain.model.PartyProfile;
import java.util.UUID;

public interface ManagePartyProfileUseCase {

  /** Consumes {@code PartyRegistered} — creates a blank, editable profile the moment the
   * party's legal identity is registered in Platform Configuration. */
  void onPartyRegistered(UUID partyId);

  PartyProfile getProfile(UUID partyId);

  PartyProfile updateProfile(UUID partyId, String history, String program, String statuteUrl, String coverUrl);
}
