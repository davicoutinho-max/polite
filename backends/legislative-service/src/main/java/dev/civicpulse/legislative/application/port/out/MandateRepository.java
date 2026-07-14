package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.Mandate;
import java.util.List;
import java.util.UUID;

public interface MandateRepository {

  Mandate save(Mandate mandate);

  List<Mandate> findByPolitician(UUID politicianAccountId);

  void deleteById(UUID id);
}
