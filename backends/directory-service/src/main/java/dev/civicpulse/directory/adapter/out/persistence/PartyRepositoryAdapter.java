package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.application.port.out.PartyRepository;
import dev.civicpulse.directory.domain.model.Party;
import dev.civicpulse.directory.domain.model.PartySpectrum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class PartyRepositoryAdapter implements PartyRepository {

  private final PartyJpaRepository jpaRepository;
  private final PartyMapper mapper;

  PartyRepositoryAdapter(PartyJpaRepository jpaRepository, PartyMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Party save(Party party) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(party)));
  }

  @Override
  public Optional<Party> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Party> search(PartySpectrum spectrum, int page, int pageSize) {
    return jpaRepository.search(spectrum, PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }
}
