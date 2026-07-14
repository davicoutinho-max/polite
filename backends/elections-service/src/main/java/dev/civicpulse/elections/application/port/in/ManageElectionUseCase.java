package dev.civicpulse.elections.application.port.in;

import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.LocalDate;
import java.util.UUID;

public interface ManageElectionUseCase {

  Election create(String title, ElectionScope scope, LocalDate electionDate, String description);

  void nominateCandidate(UUID electionId, UUID politicianAccountId);
}
