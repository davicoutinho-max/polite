package dev.civicpulse.legislative.application;

import java.util.UUID;

/** Consumed side of the politician dossier — lazily creates a stub row on the first
 * PoliticianRegistered/RepresentativeLinked event this service sees for a given account (see
 * politician_dossier_extensions' table comment in schema.sql). */
public interface DossierProjectionUseCase {

  void ensureDossierExists(UUID politicianAccountId);
}
