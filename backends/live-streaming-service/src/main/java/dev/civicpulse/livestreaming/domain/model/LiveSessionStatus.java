package dev.civicpulse.livestreaming.domain.model;

/** Mirrors {@code live_session_status_options}. Forward-only: scheduled -> live -> ended. */
public enum LiveSessionStatus {
  SCHEDULED("scheduled", 1),
  LIVE("live", 2),
  ENDED("ended", 3);

  private final String code;
  private final int sortOrder;

  LiveSessionStatus(String code, int sortOrder) {
    this.code = code;
    this.sortOrder = sortOrder;
  }

  public String code() {
    return code;
  }

  public int sortOrder() {
    return sortOrder;
  }

  public static LiveSessionStatus fromCode(String code) {
    for (LiveSessionStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown live_session_status code: " + code);
  }
}
