package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.application.port.out.PartyMemberRepository;
import dev.civicpulse.partymanagement.domain.model.PartyMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PartyMemberRepositoryAdapter implements PartyMemberRepository {

  private final PartyMemberJpaRepository jpaRepository;
  private final PartyMemberMapper mapper;

  PartyMemberRepositoryAdapter(PartyMemberJpaRepository jpaRepository, PartyMemberMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PartyMember save(PartyMember member) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(member)));
  }

  @Override
  public Optional<PartyMember> findByPartyIdAndCitizenAccountId(UUID partyId, UUID citizenAccountId) {
    return jpaRepository.findByPartyIdAndCitizenAccountId(partyId, citizenAccountId).map(mapper::toDomain);
  }

  @Override
  public List<PartyMember> findByPartyId(UUID partyId) {
    return jpaRepository.findByPartyId(partyId).stream().map(mapper::toDomain).toList();
  }
}
