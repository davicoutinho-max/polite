package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.application.port.out.PoliticianRepository;
import dev.civicpulse.directory.domain.model.GovLevel;
import dev.civicpulse.directory.domain.model.Politician;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class PoliticianRepositoryAdapter implements PoliticianRepository {

  private final PoliticianJpaRepository jpaRepository;
  private final PoliticianMapper mapper;

  PoliticianRepositoryAdapter(PoliticianJpaRepository jpaRepository, PoliticianMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Politician save(Politician politician) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(politician)));
  }

  @Override
  public Optional<Politician> findById(UUID accountId) {
    return jpaRepository.findById(accountId).map(mapper::toDomain);
  }

  @Override
  public List<Politician> search(String state, GovLevel level, UUID partyId, int page, int pageSize) {
    return jpaRepository.search(state, level, partyId, PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }

  @Override
  @Transactional
  public void assignParty(UUID accountId, UUID partyId, String partyAcronym, Instant now) {
    jpaRepository.assignParty(accountId, partyId, partyAcronym, now);
  }

  @Override
  @Transactional
  public void assignOffice(UUID accountId, String office, Instant now) {
    jpaRepository.assignOffice(accountId, office, now);
  }

  @Override
  @Transactional
  public void createIfAbsent(UUID accountId, String name, String handle, String avatarUrl, Instant now) {
    jpaRepository.createIfAbsent(accountId, name, handle, avatarUrl, now);
  }
}
