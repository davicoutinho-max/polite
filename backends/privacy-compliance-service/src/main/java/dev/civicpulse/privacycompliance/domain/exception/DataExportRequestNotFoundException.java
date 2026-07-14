package dev.civicpulse.privacycompliance.domain.exception;

import java.util.UUID;

public final class DataExportRequestNotFoundException extends RuntimeException {

  public DataExportRequestNotFoundException(UUID id) {
    super("No data export request found with id " + id);
  }
}
