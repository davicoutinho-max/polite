package dev.civicpulse.participation.application.port.in;

import dev.civicpulse.participation.domain.model.Consultation;
import dev.civicpulse.participation.domain.model.ConsultationStance;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetConsultationUseCase {

  Consultation getById(UUID id);

  List<Consultation> list(int page, int pageSize);

  Optional<ConsultationStance> getStance(UUID consultationId, UUID citizenAccountId);
}
