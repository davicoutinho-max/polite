package dev.civicpulse.participation.application.port.out;

import dev.civicpulse.participation.domain.model.ConsultationResponse;
import java.util.Optional;
import java.util.UUID;

public interface ConsultationResponseRepository {

  ConsultationResponse save(ConsultationResponse response);

  Optional<ConsultationResponse> findByConsultationAndCitizen(UUID consultationId, UUID citizenAccountId);
}
