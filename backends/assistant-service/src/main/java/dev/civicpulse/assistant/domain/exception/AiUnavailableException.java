package dev.civicpulse.assistant.domain.exception;

/** Thrown when the Gemini API key isn't configured, or the upstream call itself fails/is
 * rejected (safety block, quota, network) — always surfaced as a 503, never a 500, since it's an
 * external-dependency issue, not a bug in this service. */
public class AiUnavailableException extends RuntimeException {

  public AiUnavailableException(String message) {
    super(message);
  }

  public AiUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
