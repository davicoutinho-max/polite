package dev.civicpulse.participation.domain.exception;

public final class AlreadySignedException extends RuntimeException {

  public AlreadySignedException() {
    super("This petition is already signed by this citizen");
  }
}
