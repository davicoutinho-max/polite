package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.application.port.out.ElectionRepository;
import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class ElectionRepositoryAdapter implements ElectionRepository {

  private final ElectionJpaRepository jpaRepository;
  private final ElectionMapper mapper;

  ElectionRepositoryAdapter(ElectionJpaRepository jpaRepository, ElectionMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Election save(Election election) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(election)));
  }

  @Override
  public Optional<Election> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Election> findAll(int page, int pageSize) {
    return jpaRepository.findAllOrderByElectionDateAsc(PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Election> findByScope(ElectionScope scope, int page, int pageSize) {
    return jpaRepository.findByScope(scope, PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }

  @Override
  public List<Election> findUpcoming(LocalDate from, int page, int pageSize) {
    return jpaRepository.findUpcoming(from, PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }
}
