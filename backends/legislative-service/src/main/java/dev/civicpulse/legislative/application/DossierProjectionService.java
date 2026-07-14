package dev.civicpulse.legislative.application;

import dev.civicpulse.legislative.application.port.out.PoliticianDossierRepository;
import dev.civicpulse.legislative.domain.model.PoliticianDossierExtension;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DossierProjectionService implements DossierProjectionUseCase {

  private final PoliticianDossierRepository dossierRepository;
  private final Clock clock;

  public DossierProjectionService(PoliticianDossierRepository dossierRepository, Clock clock) {
    this.dossierRepository = dossierRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void ensureDossierExists(UUID politicianAccountId) {
    if (!dossierRepository.existsById(politicianAccountId)) {
      dossierRepository.save(PoliticianDossierExtension.createStub(politicianAccountId, clock.instant()));
    }
  }
}
