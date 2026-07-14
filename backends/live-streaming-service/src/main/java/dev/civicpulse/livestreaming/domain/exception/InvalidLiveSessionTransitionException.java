package dev.civicpulse.livestreaming.domain.exception;

import dev.civicpulse.livestreaming.domain.model.LiveSessionStatus;

public final class InvalidLiveSessionTransitionException extends RuntimeException {

  public InvalidLiveSessionTransitionException(LiveSessionStatus from, LiveSessionStatus to) {
    super("Cannot transition live session from " + from.code() + " to " + to.code());
  }
}
