package dev.civicpulse.feedcontent.domain.exception;

/** The platform's OAuth token exchange (or the app credentials themselves) failed — always a
 * real upstream problem (bad/expired code, misconfigured app, network), never a bug in our own
 * validation, so it's surfaced as 502 rather than 400/500. */
public class SocialOAuthException extends RuntimeException {

  public SocialOAuthException(String message) {
    super(message);
  }

  public SocialOAuthException(String message, Throwable cause) {
    super(message, cause);
  }
}
