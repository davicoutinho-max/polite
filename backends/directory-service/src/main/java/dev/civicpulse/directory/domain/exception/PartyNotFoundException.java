package dev.civicpulse.directory.domain.exception;

import java.util.UUID;

public final class PartyNotFoundException extends RuntimeException {

  public PartyNotFoundException(UUID id) {
    super("No party projected for id " + id);
  }
}
