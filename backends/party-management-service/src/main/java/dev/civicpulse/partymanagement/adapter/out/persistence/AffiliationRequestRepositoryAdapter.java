package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.application.port.out.AffiliationRequestRepository;
import dev.civicpulse.partymanagement.domain.model.AffiliationRequest;
import dev.civicpulse.partymanagement.domain.model.AffiliationRequestStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AffiliationRequestRepositoryAdapter implements AffiliationRequestRepository {

  private final AffiliationRequestJpaRepository jpaRepository;
  private final AffiliationRequestMapper mapper;

  AffiliationRequestRepositoryAdapter(AffiliationRequestJpaRepository jpaRepository, AffiliationRequestMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public AffiliationRequest save(AffiliationRequest request) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(request)));
  }

  @Override
  public Optional<AffiliationRequest> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<AffiliationRequest> findByPartyIdAndStatus(UUID partyId, AffiliationRequestStatus status) {
    return jpaRepository.findByPartyIdAndStatus(partyId, status).stream().map(mapper::toDomain).toList();
  }
}
