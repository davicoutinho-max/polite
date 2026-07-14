package dev.civicpulse.privacycompliance.domain.exception;

import dev.civicpulse.privacycompliance.domain.model.ExportStatus;

public final class InvalidExportTransitionException extends RuntimeException {

  public InvalidExportTransitionException(ExportStatus from, ExportStatus to) {
    super("Cannot transition data export request from " + from.code() + " to " + to.code());
  }
}
