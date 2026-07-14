package dev.civicpulse.platformconfig.domain.exception;

import java.util.UUID;

public final class PartyRegistryEntryNotFoundException extends RuntimeException {

  public PartyRegistryEntryNotFoundException(UUID id) {
    super("No party registry entry found with id " + id);
  }
}
