package dev.civicpulse.feedcontent.application.port.out;

/** Real X (Twitter) API v2 OAuth 2.0 with PKCE — the only supported user-context auth flow for
 * X's API today. Posting tweets via API requires at least X's paid "Basic" access tier (free
 * tier is read-only) — see XProperties' javadoc. */
public interface XOAuthGateway {

  /** {@code state} and {@code codeVerifier} must both be cached by the caller (keyed by state)
   * and the verifier replayed on {@link #exchangeCode} — PKCE requires the same client to start
   * and finish the flow. */
  AuthorizeRequest buildAuthorizeRequest(String state);

  /** Throws {@link dev.civicpulse.feedcontent.domain.exception.SocialOAuthException} if the code
   * or verifier is invalid/expired, or the API call itself fails. */
  XAuthResult exchangeCode(String code, String codeVerifier);

  record AuthorizeRequest(String url, String codeVerifier) {}

  record XAuthResult(String accessToken, String userId, String username) {}
}
