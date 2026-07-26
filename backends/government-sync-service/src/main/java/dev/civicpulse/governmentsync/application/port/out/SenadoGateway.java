package dev.civicpulse.governmentsync.application.port.out;

import java.util.List;

/** Senado Federal's open-data API (legis.senado.leg.br/dadosabertos) — federal upper chamber. */
public interface SenadoGateway {

  List<SenadoSenator> fetchCurrentSenators();

  /** No CPF field exists anywhere in this API (confirmed against the live current-senators
   * endpoint) — every senator requires the synthetic-document fallback, unlike deputies where
   * it's only needed for the occasional lookup failure. */
  record SenadoSenator(String externalId, String name, String partyAcronym, String state, String photoUrl, String email) {}
}
