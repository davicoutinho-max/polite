package dev.civicpulse.platformconfig.application.port.in;

import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;

public interface RegisterPartyUseCase {

  /** {@code registrationToken} — an admin-issued invite (see ManagePartyInviteUseCase) redeemed
   * here; the party's name/acronym/number/ideology/president/CNPJ all come from what the admin
   * vetted at invite time, not from this call, so the redeeming citizen can never register under
   * a different identity (or a different tax id) than the one that was approved. */
  PartyRegistryEntry registerParty(String registrationToken, String handle, String email, String rawPassword);
}
