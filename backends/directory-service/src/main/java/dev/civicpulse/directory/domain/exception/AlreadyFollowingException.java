package dev.civicpulse.directory.domain.exception;

public final class AlreadyFollowingException extends RuntimeException {

  public AlreadyFollowingException() {
    super("Already following this target");
  }
}
