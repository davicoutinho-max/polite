package dev.civicpulse.participation.application.port.in;

import dev.civicpulse.participation.domain.model.Consultation;
import java.util.List;
import java.util.UUID;

public interface GetConsultationUseCase {

  Consultation getById(UUID id);

  List<Consultation> list(int page, int pageSize);
}
