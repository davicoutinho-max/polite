package dev.civicpulse.platformconfig.application.port.out;

import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartyRegistryRepository {

  PartyRegistryEntry save(PartyRegistryEntry entry);

  Optional<PartyRegistryEntry> findById(UUID id);

  boolean existsByAcronym(String acronym);

  boolean existsByNumber(int number);

  List<PartyRegistryEntry> findAll();
}
