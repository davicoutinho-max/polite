package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.application.port.out.AffiliationRepository;
import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AffiliationRepositoryAdapter implements AffiliationRepository {

  private final AffiliationJpaRepository jpaRepository;
  private final AffiliationMapper mapper;

  AffiliationRepositoryAdapter(AffiliationJpaRepository jpaRepository, AffiliationMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Affiliation save(Affiliation affiliation) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(affiliation)));
  }

  @Override
  public Optional<Affiliation> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public boolean existsActiveByCitizenAndParty(UUID citizenAccountId, UUID partyId) {
    return jpaRepository.existsActiveByCitizenAndParty(citizenAccountId, partyId);
  }

  @Override
  public List<Affiliation> findByCitizenAccountId(UUID citizenAccountId) {
    return jpaRepository.findByCitizenAccountId(citizenAccountId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }
}
