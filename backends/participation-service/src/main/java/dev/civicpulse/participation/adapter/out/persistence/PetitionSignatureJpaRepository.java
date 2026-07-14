package dev.civicpulse.participation.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PetitionSignatureJpaRepository extends JpaRepository<PetitionSignatureJpaEntity, PetitionSignatureId> {

  boolean existsByPetitionIdAndCitizenAccountId(UUID petitionId, UUID citizenAccountId);
}
