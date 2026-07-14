package dev.civicpulse.platformconfig.domain.exception;

import java.util.UUID;

public final class CountryNotFoundException extends RuntimeException {

  public CountryNotFoundException(UUID id) {
    super("No country found with id " + id);
  }
}
