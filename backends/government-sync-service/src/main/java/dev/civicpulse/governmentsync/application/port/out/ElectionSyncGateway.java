package dev.civicpulse.governmentsync.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Calls elections-service's internal {@code POST /elections/sync} and (already-idempotent)
 * {@code POST /elections/{id}/candidacies} — ties the TSE-sourced state/municipal sync (see
 * SyncStateAndMunicipalService) into the elections calendar, which otherwise has no way to know
 * these races or their winners exist. Best-effort: never counted as a sync failure — see
 * LegislativeDossierGateway's javadoc for the identical reasoning (a brand-new politician account
 * may not be projected in directory-service yet the instant this call fires). */
public interface ElectionSyncGateway {

  void syncElectionCandidacy(
      String electionTitle, String scope, LocalDate electionDate, String location, UUID politicianAccountId);

  /** Pushes the full ranked result for one race (one office within one election) — see {@code
   * ElectionResultRepository#replaceForOffice}'s javadoc on elections-service for why this
   * replaces the whole office's result set in one call rather than upserting per candidate. */
  void syncElectionResults(
      String electionTitle, String scope, LocalDate electionDate, String location, String office, List<ResultCandidate> results);

  record ResultCandidate(
      String externalId, String candidateName, String partyAcronym, long votes, int rank, boolean elected, UUID politicianAccountId) {}
}
