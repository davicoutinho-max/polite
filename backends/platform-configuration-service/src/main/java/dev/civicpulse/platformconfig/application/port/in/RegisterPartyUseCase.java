package dev.civicpulse.platformconfig.application.port.in;

import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;

public interface RegisterPartyUseCase {

  PartyRegistryEntry registerParty(String name, String acronym, int number, String president, String ideology);
}
