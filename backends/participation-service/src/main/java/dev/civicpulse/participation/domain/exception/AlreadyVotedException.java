package dev.civicpulse.participation.domain.exception;

public final class AlreadyVotedException extends RuntimeException {

  public AlreadyVotedException() {
    super("This citizen has already voted in this survey");
  }
}
