package dev.civicpulse.governmentsync.application.port.out;

import java.util.List;
import java.util.UUID;

/** Calls legislative-service's internal {@code PUT /politicians/{id}/dossier} and
 * {@code POST /politicians/{id}/social-links} — the only government-sourced fields with anywhere
 * to land in the politician profile detail page beyond directory-service's lean read-model (see
 * that service's {@code PoliticianDossierExtension}, always created as an empty stub otherwise).
 * Best-effort: the dossier stub is created asynchronously off the same {@code RepresentativeLinked}
 * Kafka event this sync just triggered, so it may not exist yet the moment this call fires for a
 * brand-new politician — callers should treat failures here as non-fatal (see
 * SyncFederalLegislatureService), since the next day's run will find the stub already there. */
public interface LegislativeDossierGateway {

  void enrichDossier(UUID politicianAccountId, String education, String email);

  void addSocialLinks(UUID politicianAccountId, List<String> urls);
}
