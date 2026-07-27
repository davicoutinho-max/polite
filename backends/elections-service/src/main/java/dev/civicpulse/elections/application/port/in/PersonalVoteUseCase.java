package dev.civicpulse.elections.application.port.in;

import dev.civicpulse.elections.domain.model.PersonalVote;
import java.util.List;
import java.util.UUID;

public interface PersonalVoteUseCase {

  /** Idempotent per {@code (citizenAccountId, electionId, office)} — registering again for the
   * same office updates the existing pick rather than accumulating duplicates. */
  PersonalVote registerVote(
      UUID citizenAccountId, UUID electionId, String office, String candidateName, String candidatePartyAcronym, UUID politicianAccountId);

  List<PersonalVote> listVotes(UUID citizenAccountId, UUID electionId);
}
