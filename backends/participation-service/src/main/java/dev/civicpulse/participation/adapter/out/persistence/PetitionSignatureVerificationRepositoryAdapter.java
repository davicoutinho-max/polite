package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.application.port.out.PetitionSignatureVerificationRepository;
import dev.civicpulse.participation.domain.model.PendingSignatureVerification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PetitionSignatureVerificationRepositoryAdapter implements PetitionSignatureVerificationRepository {

  private final PetitionSignatureVerificationJpaRepository jpaRepository;
  private final PetitionSignatureVerificationMapper mapper;

  PetitionSignatureVerificationRepositoryAdapter(
      PetitionSignatureVerificationJpaRepository jpaRepository, PetitionSignatureVerificationMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PendingSignatureVerification save(PendingSignatureVerification verification) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(verification)));
  }

  @Override
  public Optional<PendingSignatureVerification> findById(UUID id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }
}
