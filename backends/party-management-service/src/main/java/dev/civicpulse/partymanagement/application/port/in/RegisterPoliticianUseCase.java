package dev.civicpulse.partymanagement.application.port.in;

import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.util.UUID;

public interface RegisterPoliticianUseCase {

  /** Politician self-registration by redeeming a party-issued invite token (flow 02): provisions
   * the identity via Identity Service, then links the new account as a representative of
   * {@code partyId}. The politician's name/role/state come from what the party vetted at invite
   * time (see ManagePoliticianInviteUseCase), not from this call — only the credentials
   * (email/password/document) are the politician's own. Rejects a token issued by a different
   * party than {@code partyId}. */
  PartyRepresentative registerPolitician(UUID partyId, RegisterPoliticianCommand command);

  record RegisterPoliticianCommand(
      String registrationToken, String handle, String email, String rawPassword, String documentType, String rawDocumentNumber) {}
}
