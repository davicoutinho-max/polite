package dev.civicpulse.governmentsync.application.port.out;

import java.util.List;

/** Câmara dos Deputados' open-data API (dadosabertos.camara.leg.br) — federal chamber. */
public interface CamaraGateway {

  List<CamaraParty> fetchAllParties();

  List<CamaraDeputy> fetchAllDeputies();

  /** {@code electoralNumber} is frequently null in the API itself (confirmed even for well-known
   * parties like MDB) — callers must be prepared to fall back when absent. */
  record CamaraParty(String externalId, String acronym, String name, String logoUrl, Integer electoralNumber) {}

  /** {@code cpf}/{@code education}/{@code socialLinks}/{@code phone}/{@code officeDetail}/
   * {@code mandateStartDate} are only available from the per-deputy detail endpoint
   * ({@code ultimoStatus}/{@code ultimoStatus.gabinete}), not the list endpoint — left null/empty
   * here when the detail lookup fails, or when the deputy has no assigned office yet (confirmed
   * null for out-of-mandate deputies), in which case the caller must apply the same
   * synthetic-document fallback used elsewhere in this service for {@code cpf}, and simply leave
   * the dossier fields unenriched for the rest. {@code mandateStartDate} is an ISO date
   * (e.g. "2023-02-01"), matching {@code ultimoStatus.data}. */
  record CamaraDeputy(
      String externalId,
      String name,
      String partyAcronym,
      String state,
      String photoUrl,
      String email,
      String cpf,
      String education,
      List<String> socialLinks,
      String phone,
      String officeDetail,
      String mandateStartDate) {}
}
