package dev.civicpulse.feedcontent.domain.model;

/** Mirrors {@code social_share_status_options}. */
public enum ShareStatus {
  PUBLISHED("published"),
  FAILED("failed");

  private final String code;

  ShareStatus(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static ShareStatus fromCode(String code) {
    for (ShareStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown social_share_status code: " + code);
  }
}
