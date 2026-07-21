package dev.civicpulse.partymanagement.application.port.out;

import dev.civicpulse.partymanagement.domain.model.PartyProfile;
import java.util.Optional;
import java.util.UUID;

public interface PartyProfileRepository {

  PartyProfile save(PartyProfile profile);

  Optional<PartyProfile> findByPartyId(UUID partyId);

  boolean existsByPartyId(UUID partyId);

  void deleteByPartyId(UUID partyId);
}
