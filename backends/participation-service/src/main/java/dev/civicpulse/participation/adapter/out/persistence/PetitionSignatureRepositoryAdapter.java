package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.application.port.out.PetitionSignatureRepository;
import dev.civicpulse.participation.domain.model.PetitionSignature;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class PetitionSignatureRepositoryAdapter implements PetitionSignatureRepository {

  private final PetitionSignatureJpaRepository jpaRepository;
  private final PetitionSignatureMapper mapper;

  PetitionSignatureRepositoryAdapter(PetitionSignatureJpaRepository jpaRepository, PetitionSignatureMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public PetitionSignature save(PetitionSignature signature) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(signature)));
  }

  @Override
  public boolean exists(UUID petitionId, UUID citizenAccountId) {
    return jpaRepository.existsByPetitionIdAndCitizenAccountId(petitionId, citizenAccountId);
  }
}
