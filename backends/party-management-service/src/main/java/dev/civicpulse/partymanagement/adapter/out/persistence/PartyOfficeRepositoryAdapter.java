package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.application.port.out.PartyOfficeRepository;
import dev.civicpulse.partymanagement.domain.model.PartyOffice;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PartyOfficeRepositoryAdapter implements PartyOfficeRepository {

  private final PartyOfficeJpaRepository jpaRepository;
  private final PartyOfficeMapper mapper;

  PartyOfficeRepositoryAdapter(PartyOfficeJpaRepository jpaRepository, PartyOfficeMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PartyOffice save(PartyOffice office) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(office)));
  }

  @Override
  public Optional<PartyOffice> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<PartyOffice> findByPartyId(UUID partyId) {
    return jpaRepository.findByPartyId(partyId).stream().map(mapper::toDomain).toList();
  }
}
