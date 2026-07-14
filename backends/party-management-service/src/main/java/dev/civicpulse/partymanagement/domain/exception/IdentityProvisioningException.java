package dev.civicpulse.partymanagement.domain.exception;

/** Wraps a failure from {@link dev.civicpulse.partymanagement.application.port.out.IdentityProvisioningGateway}
 * — e.g. Identity Service rejected the registration (duplicate email/handle/CPF, invalid
 * document number) or was unreachable. */
public final class IdentityProvisioningException extends RuntimeException {

  public IdentityProvisioningException(String message) {
    super(message);
  }

  public IdentityProvisioningException(String message, Throwable cause) {
    super(message, cause);
  }
}
