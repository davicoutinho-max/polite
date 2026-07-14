package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.application.port.out.PetitionRepository;
import dev.civicpulse.participation.domain.model.Petition;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class PetitionRepositoryAdapter implements PetitionRepository {

  private final PetitionJpaRepository jpaRepository;
  private final PetitionMapper mapper;

  PetitionRepositoryAdapter(PetitionJpaRepository jpaRepository, PetitionMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Petition save(Petition petition) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(petition)));
  }

  @Override
  public Optional<Petition> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Petition> findAll(int page, int pageSize) {
    return jpaRepository.findAllOrdered(PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }
}
