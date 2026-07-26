package dev.civicpulse.platformconfig.application.port.out;

import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartyRegistryRepository {

  PartyRegistryEntry save(PartyRegistryEntry entry);

  Optional<PartyRegistryEntry> findById(UUID id);

  /** Upsert-by-acronym lookup for the government-data sync flow — see SyncPartyUseCase. */
  Optional<PartyRegistryEntry> findByAcronym(String acronym);

  boolean existsByAcronym(String acronym);

  boolean existsByNumber(int number);

  List<PartyRegistryEntry> findAll();
}
