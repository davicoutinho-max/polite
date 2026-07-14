package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.application.port.out.MandateRepository;
import dev.civicpulse.legislative.domain.model.Mandate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class MandateRepositoryAdapter implements MandateRepository {

  private final MandateJpaRepository jpaRepository;
  private final MandateMapper mapper;

  MandateRepositoryAdapter(MandateJpaRepository jpaRepository, MandateMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Mandate save(Mandate mandate) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(mandate)));
  }

  @Override
  public List<Mandate> findByPolitician(UUID politicianAccountId) {
    return jpaRepository.findByPoliticianAccountId(politicianAccountId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
