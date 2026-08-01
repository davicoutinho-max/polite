package dev.civicpulse.membershipaffiliation.application.port.in;

import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatusHistoryEntry;
import dev.civicpulse.membershipaffiliation.domain.model.MembershipCard;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManageAffiliationUseCase {

  /** Citizen-initiated (flow 04) — {@code city} is pass-through data for the {@code
   * AffiliationRequested} event; this service doesn't persist it locally. The remaining
   * parameters are the real TSE-required voter-registration and identity-check data (see
   * Affiliation's javadoc) and are persisted. */
  Affiliation requestAffiliation(
      UUID citizenAccountId,
      UUID partyId,
      String city,
      String voterRegistrationNumber,
      String electoralZone,
      String electoralSection,
      String electoralState,
      String electoralMunicipality,
      String identityPhotoUrl);

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

  /** The digital membership card issued alongside {@link #confirmAffiliation}, if any. */
  Optional<MembershipCard> getCard(UUID affiliationId);

  /** Full audit trail, oldest first — the citizen-facing status timeline reads directly off this
   * rather than just the affiliation's current status, since {@code fromStatus}/{@code
   * changedBy}/{@code changedAt} per step is what makes a real timeline instead of a bare badge. */
  List<AffiliationStatusHistoryEntry> listStatusHistory(UUID affiliationId);
}
