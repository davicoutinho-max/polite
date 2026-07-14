package dev.civicpulse.participation.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ConsultationResponseJpaRepository extends JpaRepository<ConsultationResponseJpaEntity, ConsultationResponseId> {

  Optional<ConsultationResponseJpaEntity> findByConsultationIdAndCitizenAccountId(UUID consultationId, UUID citizenAccountId);
}
