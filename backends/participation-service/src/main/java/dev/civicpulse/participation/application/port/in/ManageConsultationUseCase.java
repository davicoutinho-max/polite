package dev.civicpulse.participation.application.port.in;

import dev.civicpulse.participation.domain.model.Consultation;
import dev.civicpulse.participation.domain.model.ConsultationStance;
import java.time.LocalDate;
import java.util.UUID;

public interface ManageConsultationUseCase {

  Consultation create(String title, String description, LocalDate deadline);

  void respond(UUID consultationId, UUID citizenAccountId, ConsultationStance stance);
}
