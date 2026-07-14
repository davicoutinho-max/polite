package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.application.port.out.PartyProfileRepository;
import dev.civicpulse.partymanagement.domain.model.PartyProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PartyProfileRepositoryAdapter implements PartyProfileRepository {

  private final PartyProfileJpaRepository jpaRepository;
  private final PartyProfileMapper mapper;

  PartyProfileRepositoryAdapter(PartyProfileJpaRepository jpaRepository, PartyProfileMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PartyProfile save(PartyProfile profile) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(profile)));
  }

  @Override
  public Optional<PartyProfile> findByPartyId(UUID partyId) {
    return jpaRepository.findById(partyId).map(mapper::toDomain);
  }

  @Override
  public boolean existsByPartyId(UUID partyId) {
    return jpaRepository.existsById(partyId);
  }
}
