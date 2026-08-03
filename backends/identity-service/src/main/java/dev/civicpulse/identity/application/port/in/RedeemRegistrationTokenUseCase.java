package dev.civicpulse.identity.application.port.in;

import dev.civicpulse.identity.domain.model.RegistrationToken;

/** Redeeming side of the invite-token flow. {@link #validate} is read-only (the register page
 * uses it to preview the invite and prefill its form before the person has typed anything
 * committal); {@link #redeem} permanently consumes it and is only ever called once, right before
 * the redeeming service (platform-configuration-service or party-management-service) creates the
 * actual account — see RegistrationToken's javadoc for why the account itself is created
 * elsewhere, not here. */
public interface RedeemRegistrationTokenUseCase {

  RegistrationToken validate(String rawToken);

  RegistrationToken redeem(String rawToken);
}
