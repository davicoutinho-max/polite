package dev.civicpulse.participation.application.port.in;

import dev.civicpulse.participation.domain.model.Petition;
import java.time.LocalDate;
import java.util.UUID;

public interface ManagePetitionUseCase {

  Petition create(String title, String summary, String category, int goal, LocalDate deadline);

  void sign(UUID petitionId, UUID citizenAccountId);
}
