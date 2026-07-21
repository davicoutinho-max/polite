package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.PoliticianDossierRepository;
import dev.civicpulse.legislative.domain.model.PoliticianDossierExtension;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PoliticianDossierExtensionRepositoryAdapter implements PoliticianDossierRepository {

  private final PoliticianDossierExtensionJpaRepository jpaRepository;
  private final PoliticianDossierExtensionMapper mapper;

  PoliticianDossierExtensionRepositoryAdapter(PoliticianDossierExtensionJpaRepository jpaRepository, PoliticianDossierExtensionMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PoliticianDossierExtension save(PoliticianDossierExtension dossier) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(dossier)));
  }

  @Override
  public Optional<PoliticianDossierExtension> findById(UUID politicianAccountId) {
    return jpaRepository.findById(politicianAccountId).map(mapper::toDomain);
  }

  @Override
  public boolean existsById(UUID politicianAccountId) {
    return jpaRepository.existsById(politicianAccountId);
  }

  @Override
  public void deleteById(UUID politicianAccountId) {
    jpaRepository.deleteById(politicianAccountId);
  }
}
