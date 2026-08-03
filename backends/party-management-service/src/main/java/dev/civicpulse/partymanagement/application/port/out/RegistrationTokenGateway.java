package dev.civicpulse.partymanagement.application.port.out;

import java.util.List;
import java.util.UUID;

/** Anti-corruption-layer boundary onto Identity Service's invite-token subsystem — see its
 * RegistrationToken domain javadoc. Politician registration used to let a party admin type the
 * new politician's password directly (flow 02); it now redeems a token the party issued earlier
 * instead, so only the politician themselves ever knows their own password. */
public interface RegistrationTokenGateway {

  /** Issues a new politician-invite token, emailed to {@code targetEmail}. {@code
   * prefillDataJson} is this service's own opaque encoding of the invite's vetted fields
   * (name/roleTitle/state/partyId) — Identity Service stores and returns it verbatim. */
  IssuedToken issueForPolitician(UUID issuedByPartyAccountId, String targetEmail, String prefillDataJson);

  IssuedToken resend(UUID tokenId, UUID issuedByPartyAccountId);

  List<IssuedToken> listIssuedBy(UUID issuedByPartyAccountId);

  /** Read-only — throws if the token is missing, expired, already used, or not actually a
   * politician token. Called before {@link IdentityProvisioningGateway} provisions the account,
   * so a provisioning failure never burns the token — see RegisterPoliticianService. */
  RedeemedToken validate(String rawToken);

  /** Permanently marks the token consumed. Called only after the account was successfully
   * provisioned — see RegisterPoliticianService. */
  void consume(String rawToken);

  record IssuedToken(UUID id, String token, String targetEmail, String prefillDataJson, String status) {}

  record RedeemedToken(String prefillDataJson) {}
}
