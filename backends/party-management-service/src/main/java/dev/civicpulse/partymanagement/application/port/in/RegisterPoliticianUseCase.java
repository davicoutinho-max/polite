package dev.civicpulse.partymanagement.application.port.in;

import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.util.UUID;

public interface RegisterPoliticianUseCase {

  /** Party-initiated politician registration (flow 02): provisions the identity via Identity
   * Service, then links the new account as a representative of {@code partyId}. Politicians
   * are never self-service — see docs/architecture (party-only registration). */
  PartyRepresentative registerPolitician(UUID partyId, RegisterPoliticianCommand command);

  record RegisterPoliticianCommand(
      String name,
      String handle,
      String email,
      String rawPassword,
      String documentType,
      String rawDocumentNumber,
      String roleTitle) {}
}
