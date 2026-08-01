package dev.civicpulse.legislative.application.port.out;

/** Anti-corruption-layer port to assistant-service's real Gemini-backed document understanding —
 * a real synchronous REST call (see party-management-service's IdentityProvisioningGateway for
 * the established pattern), not a stub. Downloads the attached document itself and asks the
 * model to read it and compare its total against the declared amount. */
public interface DocumentVerificationGateway {

  /** Throws {@link dev.civicpulse.legislative.domain.exception.DocumentVerificationException} if
   * assistant-service is unreachable or the AI call itself fails — a real "does this document
   * support the amount" answer (yes or no) is always a {@link Result}, never an exception. */
  Result verify(String documentUrl, String categoryLabel, long declaredAmountCents);

  record Result(boolean matches, Long extractedAmountCents, String feedback) {}
}
