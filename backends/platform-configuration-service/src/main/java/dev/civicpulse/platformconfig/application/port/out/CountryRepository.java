package dev.civicpulse.platformconfig.application.port.out;

import dev.civicpulse.platformconfig.domain.model.Country;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CountryRepository {

  Country save(Country country);

  void delete(UUID id);

  Optional<Country> findById(UUID id);

  List<Country> findAll();
}
