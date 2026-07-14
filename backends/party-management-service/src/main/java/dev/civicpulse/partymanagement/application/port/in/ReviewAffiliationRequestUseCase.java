package dev.civicpulse.partymanagement.application.port.in;

import dev.civicpulse.partymanagement.domain.model.AffiliationRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReviewAffiliationRequestUseCase {

  /** Consumes {@code AffiliationRequested} — the multi-stage saga itself is owned by
   * Membership &amp; Affiliation; this just creates the party-side review row. */
  void onAffiliationRequested(UUID requestId, UUID partyId, UUID citizenAccountId, String city, Instant requestedAt);

  /** Approving admits the citizen as an active {@link dev.civicpulse.partymanagement.domain.model.PartyMember}
   * in the same transaction. */
  AffiliationRequest approve(UUID requestId);

  AffiliationRequest reject(UUID requestId);

  List<AffiliationRequest> listPending(UUID partyId);
}
