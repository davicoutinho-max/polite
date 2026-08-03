package dev.civicpulse.directory.application.port.in;

import java.util.UUID;

/** Self-service update of a party's own name/acronym/number/ideology/founded year/president —
 * see {@code Party.updateDetails}'s javadoc for why this exists alongside the government-sync
 * projection that normally owns these fields. */
public interface UpdatePartyDetailsUseCase {

  void updatePartyDetails(UUID partyId, String name, String acronym, int number, String ideology, Integer foundedYear, String president);
}
