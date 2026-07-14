package dev.civicpulse.platformconfig.adapter.out.persistence;

import dev.civicpulse.platformconfig.application.port.out.PartyRegistryRepository;
import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PartyRegistryRepositoryAdapter implements PartyRegistryRepository {

  private final PartyRegistryJpaRepository jpaRepository;
  private final PartyRegistryMapper mapper;

  PartyRegistryRepositoryAdapter(PartyRegistryJpaRepository jpaRepository, PartyRegistryMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PartyRegistryEntry save(PartyRegistryEntry entry) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(entry)));
  }

  @Override
  public Optional<PartyRegistryEntry> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public boolean existsByAcronym(String acronym) {
    return jpaRepository.existsByAcronym(acronym);
  }

  @Override
  public boolean existsByNumber(int number) {
    return jpaRepository.existsByNumber(number);
  }

  @Override
  public List<PartyRegistryEntry> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
  }
}
