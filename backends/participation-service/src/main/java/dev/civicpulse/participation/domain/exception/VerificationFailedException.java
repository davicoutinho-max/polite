package dev.civicpulse.participation.domain.exception;

/** Covers every way a "confirm signature" call can fail against its pending verification record —
 * not found, already consumed, expired, or a wrong code — all of which are the same class of
 * problem to the caller (start over) and don't need distinct handling. */
public final class VerificationFailedException extends RuntimeException {

  public VerificationFailedException(String reason) {
    super(reason);
  }
}
