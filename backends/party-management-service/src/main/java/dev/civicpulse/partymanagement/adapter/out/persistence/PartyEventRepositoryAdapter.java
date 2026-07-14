package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.application.port.out.PartyEventRepository;
import dev.civicpulse.partymanagement.domain.model.PartyEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PartyEventRepositoryAdapter implements PartyEventRepository {

  private final PartyEventJpaRepository jpaRepository;
  private final PartyEventMapper mapper;

  PartyEventRepositoryAdapter(PartyEventJpaRepository jpaRepository, PartyEventMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PartyEvent save(PartyEvent event) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(event)));
  }

  @Override
  public List<PartyEvent> findByPartyId(UUID partyId) {
    return jpaRepository.findByPartyIdOrderByEventDateAsc(partyId).stream().map(mapper::toDomain).toList();
  }
}
