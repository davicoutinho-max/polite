package dev.civicpulse.platformconfig.application.port.in;

import dev.civicpulse.platformconfig.domain.model.Country;
import dev.civicpulse.platformconfig.domain.model.State;
import java.util.List;
import java.util.UUID;

public interface ManageGeographyUseCase {

  Country addCountry(String name, String code);

  void removeCountry(UUID id);

  List<Country> listCountries();

  State addState(UUID countryId, String name, String code);

  List<State> listStates(UUID countryId);
}
