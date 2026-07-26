package dev.civicpulse.governmentsync.application.port.out;

import java.util.List;
import java.util.Set;

/** dadosabertos.tse.jus.br's {@code votacao_candidato_munzona} bulk dataset — TSE's only source
 * for state/municipal elected officials (no live query API exists, unlike Câmara/Senado). Confirmed
 * by direct download+inspection (2022 general election, 2024 municipal election, Sergipe and DF
 * samples): one row per (candidate, município, zona eleitoral) — the same candidate repeats across
 * every município/zona they received any votes in, so callers get pre-deduplicated-by-candidate
 * results here, not raw rows. No CPF, no photo, no email anywhere in this dataset — every result
 * needs the same synthetic-document/synthetic-email treatment as Senado's federal senators. */
public interface TseElectionDataGateway {

  /** {@code cargoFilter} must use TSE's own {@code DS_CARGO} strings exactly (confirmed values:
   * "Governador", "Deputado Estadual", "Deputado Distrital" for the 2022 dataset; "Prefeito",
   * "Vereador" for the 2024 dataset — "Vice-Governador"/"Vice-Prefeito" never appear as separate
   * rows in this file at all, since running mates don't receive separate nominal votes). Only
   * candidates whose {@code CD_SIT_TOT_TURNO} marks them elected (codes 1/2/3 — confirmed via a
   * live sample: 1=ELEITO, 2=ELEITO POR QP, 3=ELEITO POR MÉDIA, 4=NÃO ELEITO, 5=SUPLENTE) are
   * returned. */
  List<TseElectedCandidate> fetchElectedCandidates(int year, String uf, Set<String> cargoFilter);

  /** {@code municipality} is only meaningful for municipal cargos (a Vereador's own city); for
   * state cargos it's whatever município happened to appear last while deduplicating rows across
   * the state — callers must use {@code uf}, not {@code municipality}, as the location for those. */
  record TseElectedCandidate(
      String externalId,
      String legalName,
      String ballotName,
      String cargo,
      String partyAcronym,
      Integer partyNumber,
      String partyName,
      String uf,
      String municipality) {}
}
