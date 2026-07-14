package dev.civicpulse.membershipaffiliation.application.port.in;

import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import java.util.List;
import java.util.UUID;

public interface ManageAffiliationUseCase {

  /** Citizen-initiated (flow 04) — {@code city} is pass-through data for the {@code
   * AffiliationRequested} event; this service doesn't persist it locally. */
  Affiliation requestAffiliation(UUID citizenAccountId, UUID partyId, String city);

  /** Consumes {@code AffiliationRequestApproved} — advances REQUESTED/UNDER_REVIEW to
   * PARTY_APPROVED. */
  void onAffiliationRequestApproved(UUID affiliationId);

  /** Consumes {@code AffiliationRequestRejected}. */
  void onAffiliationRequestRejected(UUID affiliationId);

  /** Simulates the external Electoral Justice authority's intake — see
   * StubDocumentVerificationGatewayAdapter in identity-service for the same
   * anti-corruption-layer stub pattern (no real integration exists to drive this). */
  Affiliation sendToElectoralJustice(UUID affiliationId);

  /** Simulates the external Electoral Justice authority's final confirmation — issues the
   * membership card as part of the same transaction. */
  Affiliation confirmAffiliation(UUID affiliationId);

  Affiliation getById(UUID id);

  List<Affiliation> listByCitizen(UUID citizenAccountId);
}
