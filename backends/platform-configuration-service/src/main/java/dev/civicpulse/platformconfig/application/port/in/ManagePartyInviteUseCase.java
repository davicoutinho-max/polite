package dev.civicpulse.platformconfig.application.port.in;

import dev.civicpulse.platformconfig.application.port.out.RegistrationTokenGateway.IssuedToken;
import java.util.List;
import java.util.UUID;

/** Platform-admin side of party self-registration: issuing/listing/resending invite tokens for
 * vetted parties, so the party's own contact can redeem one and set their own password — see
 * RegistrationTokenGateway's javadoc for the full picture. */
public interface ManagePartyInviteUseCase {

  IssuedToken issue(UUID issuedByAdminAccountId, String targetEmail, PartyInvitePrefill prefill);

  IssuedToken resend(UUID tokenId, UUID issuedByAdminAccountId);

  List<IssuedToken> listIssuedBy(UUID issuedByAdminAccountId);

  /** The party's vetted identity fields, decided by the admin at invite time — the redeeming
   * citizen never gets to type a different name/acronym/number/CNPJ than what was approved here.
   * CNPJ is included on purpose (not left to the redeemer to self-report, unlike a politician's
   * personal CPF): a party is a pessoa jurídica under Lei 9.096/95, so its tax id is exactly the
   * kind of fact the admin should already have on file when vetting a real party, not something
   * an anonymous invite redeemer gets to declare unverified. */
  record PartyInvitePrefill(String name, String acronym, Integer number, String ideology, String president, String cnpj) {}
}
