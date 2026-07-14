package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.application.port.out.PartyRepresentativeRepository;
import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PartyRepresentativeRepositoryAdapter implements PartyRepresentativeRepository {

  private final PartyRepresentativeJpaRepository jpaRepository;
  private final PartyRepresentativeMapper mapper;

  PartyRepresentativeRepositoryAdapter(PartyRepresentativeJpaRepository jpaRepository, PartyRepresentativeMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PartyRepresentative save(PartyRepresentative representative) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(representative)));
  }

  @Override
  public void delete(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public Optional<PartyRepresentative> findByPartyIdAndPoliticianAccountId(UUID partyId, UUID politicianAccountId) {
    return jpaRepository.findByPartyIdAndPoliticianAccountId(partyId, politicianAccountId).map(mapper::toDomain);
  }

  @Override
  public boolean existsByPartyIdAndPoliticianAccountId(UUID partyId, UUID politicianAccountId) {
    return jpaRepository.existsByPartyIdAndPoliticianAccountId(partyId, politicianAccountId);
  }

  @Override
  public List<PartyRepresentative> findByPartyId(UUID partyId) {
    return jpaRepository.findByPartyId(partyId).stream().map(mapper::toDomain).toList();
  }
}
