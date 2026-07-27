package dev.civicpulse.elections.application.port.out;

import dev.civicpulse.elections.domain.model.ElectionResult;
import java.util.List;
import java.util.UUID;

public interface ElectionResultRepository {

  /** Wholesale replace of every result row under {@code (electionId, office)} — safe to call once
   * per race per sync run without disturbing other offices bundled under the same Election (e.g.
   * Governador vs Deputado Estadual results both living under one "Eleições Estaduais 2022 — SE"
   * election). The sync always recomputes the full ranked list for a race in one pass, so
   * delete-then-insert is simpler and just as correct as a per-candidate incremental upsert. */
  void replaceForOffice(UUID electionId, String office, List<ElectionResult> results);

  /** Ordered by office, then rank — callers rendering "who won / 2nd / 3rd" per race can consume
   * this directly without re-sorting. */
  List<ElectionResult> findByElectionId(UUID electionId);
}
