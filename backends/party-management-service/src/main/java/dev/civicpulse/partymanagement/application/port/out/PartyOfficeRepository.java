package dev.civicpulse.partymanagement.application.port.out;

import dev.civicpulse.partymanagement.domain.model.PartyOffice;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartyOfficeRepository {

  PartyOffice save(PartyOffice office);

  Optional<PartyOffice> findById(UUID id);

  List<PartyOffice> findByPartyId(UUID partyId);
}
