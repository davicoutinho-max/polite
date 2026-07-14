package dev.civicpulse.platformconfig.application.port.out;

import dev.civicpulse.platformconfig.domain.model.PoliticianAssignment;
import java.util.Optional;
import java.util.UUID;

public interface PoliticianAssignmentRepository {

  PoliticianAssignment save(PoliticianAssignment assignment);

  Optional<PoliticianAssignment> findById(UUID politicianAccountId);
}
