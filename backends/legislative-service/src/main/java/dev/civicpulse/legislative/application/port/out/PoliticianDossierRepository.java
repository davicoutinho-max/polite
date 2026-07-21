package dev.civicpulse.legislative.application.port.out;

import dev.civicpulse.legislative.domain.model.PoliticianDossierExtension;
import java.util.Optional;
import java.util.UUID;

public interface PoliticianDossierRepository {

  PoliticianDossierExtension save(PoliticianDossierExtension dossier);

  Optional<PoliticianDossierExtension> findById(UUID politicianAccountId);

  boolean existsById(UUID politicianAccountId);

  void deleteById(UUID politicianAccountId);
}
