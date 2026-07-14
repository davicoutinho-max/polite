package dev.civicpulse.participation.application.port.out;

import dev.civicpulse.participation.domain.model.Consultation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsultationRepository {

  Consultation save(Consultation consultation);

  Optional<Consultation> findById(UUID id);

  List<Consultation> findAll(int page, int pageSize);
}
