package dev.civicpulse.partymanagement.application.port.in;

import dev.civicpulse.partymanagement.application.port.out.RegistrationTokenGateway.IssuedToken;
import java.util.List;
import java.util.UUID;

/** Party-admin side of politician self-registration: issuing/listing/resending invite tokens for
 * politicians the party wants to register, so the politician's own contact can redeem one and set
 * their own password — see RegistrationTokenGateway's javadoc for the full picture. */
public interface ManagePoliticianInviteUseCase {

  IssuedToken issue(UUID partyId, String targetEmail, PoliticianInvitePrefill prefill);

  IssuedToken resend(UUID partyId, UUID tokenId);

  List<IssuedToken> listIssuedBy(UUID partyId);

  /** The politician's vetted fields, decided by the party at invite time — the redeeming citizen
   * never gets to type a different name/role/state than what the party approved, and can only
   * ever end up linked to the exact party that issued the token. */
  record PoliticianInvitePrefill(String name, String roleTitle, String state, UUID partyId) {}
}
