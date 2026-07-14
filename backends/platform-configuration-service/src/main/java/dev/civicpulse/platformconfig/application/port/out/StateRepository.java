package dev.civicpulse.platformconfig.application.port.out;

import dev.civicpulse.platformconfig.domain.model.State;
import java.util.List;
import java.util.UUID;

public interface StateRepository {

  State save(State state);

  List<State> findByCountryId(UUID countryId);
}
