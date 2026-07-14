package dev.civicpulse.elections.application.port.in;

import dev.civicpulse.elections.application.port.out.PoliticianDirectoryGateway.PoliticianSummary;
import dev.civicpulse.elections.domain.model.Election;
import dev.civicpulse.elections.domain.model.ElectionScope;
import java.util.List;
import java.util.UUID;

public interface GetElectionUseCase {

  Election getById(UUID id);

  List<Election> list(ElectionScope scope, int page, int pageSize);

  List<Election> listUpcoming(int page, int pageSize);

  /** Resolves each candidacy's politician profile against Directory Service; a candidacy whose
   * politician can no longer be found there is silently omitted rather than failing the whole
   * list. */
  List<PoliticianSummary> listCandidates(UUID electionId);
}
