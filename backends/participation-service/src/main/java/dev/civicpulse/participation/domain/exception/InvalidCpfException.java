package dev.civicpulse.participation.domain.exception;

public final class InvalidCpfException extends RuntimeException {

  public InvalidCpfException() {
    super("CPF is not a valid Brazilian document number");
  }
}
