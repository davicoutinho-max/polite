package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.application.port.out.ConsultationRepository;
import dev.civicpulse.participation.domain.model.Consultation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
class ConsultationRepositoryAdapter implements ConsultationRepository {

  private final ConsultationJpaRepository jpaRepository;
  private final ConsultationMapper mapper;

  ConsultationRepositoryAdapter(ConsultationJpaRepository jpaRepository, ConsultationMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Consultation save(Consultation consultation) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(consultation)));
  }

  @Override
  public Optional<Consultation> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Consultation> findAll(int page, int pageSize) {
    return jpaRepository.findAllOrdered(PageRequest.of(page, pageSize)).stream().map(mapper::toDomain).toList();
  }
}
