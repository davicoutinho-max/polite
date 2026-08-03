package dev.civicpulse.identity.application.port.out;

/** Transactional email — mirrors participation-service's identically-named port for petition
 * signature codes. Kept as its own small adapter here rather than shared, matching this
 * codebase's hexagonal convention of no cross-service library reuse. */
public interface EmailGateway {

  /** {@code rawToken} — the adapter builds the actual clickable URL from its own configured
   * base URL, so the application layer never needs to know the frontend's shape. */
  void sendRegistrationInvite(String toEmail, String rawToken, String accountTypeLabel);
}
