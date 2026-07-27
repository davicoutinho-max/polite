package dev.civicpulse.elections.application.port.in;

import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ManageElectionUseCase {

  Election create(String title, ElectionScope scope, LocalDate electionDate, String description);

  /** Government-data-sync path (see government-sync-service) — upserts by
   * {@code (scope, electionDate, location)} rather than requiring a caller-supplied id: a first
   * call creates the election, a repeat call for the same race returns the existing one untouched
   * (title/description are trusted from whichever sync run created it first — see
   * platform-configuration-service's {@code SyncPartyService} for the identical "first writer
   * wins" precedent). */
  Election syncElection(String title, ElectionScope scope, LocalDate electionDate, String location, String description);

  void nominateCandidate(UUID electionId, UUID politicianAccountId);

  /** Wholesale replace of every result under {@code (electionId, office)} — see
   * {@code ElectionResultRepository#replaceForOffice} for why this isn't an incremental upsert. */
  void syncElectionResults(UUID electionId, String office, List<ResultInput> results);

  record ResultInput(
      String externalId, String candidateName, String partyAcronym, long votes, int rank, boolean elected, UUID politicianAccountId) {}
}
