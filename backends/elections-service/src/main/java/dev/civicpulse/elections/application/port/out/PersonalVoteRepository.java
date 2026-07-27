package dev.civicpulse.elections.application.port.out;

import dev.civicpulse.elections.domain.model.PersonalVote;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonalVoteRepository {

  Optional<PersonalVote> findByCitizenElectionAndOffice(UUID citizenAccountId, UUID electionId, String office);

  /** Ordered however the caller wants to group/display them — the query side (see
   * PersonalVoteQueryService) groups by office itself, so no ordering guarantee is made here. */
  List<PersonalVote> findByCitizenAndElection(UUID citizenAccountId, UUID electionId);

  PersonalVote save(PersonalVote vote);
}
