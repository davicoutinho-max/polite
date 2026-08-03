package dev.civicpulse.platformconfig.application.port.out;

import java.util.UUID;

/** Anti-corruption-layer boundary onto Identity Service's invite-token subsystem — see its
 * RegistrationToken domain javadoc. Party registration used to let a platform admin type the new
 * party's identity fields and password directly; it now redeems a token an admin issued earlier
 * instead, so only the party's own contact ever knows their password. */
public interface RegistrationTokenGateway {

  /** Issues a new party-invite token, emailed to {@code targetEmail}. {@code prefillDataJson} is
   * this service's own opaque encoding of the party's vetted identity fields (name/acronym/
   * number/ideology/president) — Identity Service stores and returns it verbatim. */
  IssuedToken issueForParty(UUID issuedByAdminAccountId, String targetEmail, String prefillDataJson);

  IssuedToken resend(UUID tokenId, UUID issuedByAdminAccountId);

  java.util.List<IssuedToken> listIssuedBy(UUID issuedByAdminAccountId);

  /** Read-only — throws if the token is missing, expired, already used, or not actually a party
   * token. Called before {@link IdentityProvisioningGateway} provisions the account, so a
   * provisioning failure never burns the token — see RegisterPartyService. */
  RedeemedToken validate(String rawToken);

  /** Permanently marks the token consumed. Called only after the account was successfully
   * provisioned — see RegisterPartyService. */
  void consume(String rawToken);

  record IssuedToken(UUID id, String token, String targetEmail, String prefillDataJson, String status) {}

  record RedeemedToken(String prefillDataJson) {}
}
