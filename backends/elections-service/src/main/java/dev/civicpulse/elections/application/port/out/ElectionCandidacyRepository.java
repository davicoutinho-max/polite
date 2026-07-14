package dev.civicpulse.elections.application.port.out;

import dev.civicpulse.elections.domain.model.ElectionCandidacy;
import java.util.List;
import java.util.UUID;

public interface ElectionCandidacyRepository {

  ElectionCandidacy save(ElectionCandidacy candidacy);

  List<ElectionCandidacy> findByElectionId(UUID electionId);

  boolean exists(UUID electionId, UUID politicianAccountId);
}
