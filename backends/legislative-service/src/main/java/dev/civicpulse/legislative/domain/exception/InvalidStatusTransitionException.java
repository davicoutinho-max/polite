package dev.civicpulse.legislative.domain.exception;

import dev.civicpulse.legislative.domain.model.LegislativeItemStatus;

public final class InvalidStatusTransitionException extends RuntimeException {

  public InvalidStatusTransitionException(LegislativeItemStatus from, LegislativeItemStatus to) {
    super("Cannot transition legislative item status from " + from + " to " + to);
  }
}
