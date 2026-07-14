package dev.civicpulse.membershipaffiliation.domain.model;

/** Mirrors {@code fee_status_options}. */
public enum FeeStatus {
  PENDING("pending"),
  PAID("paid"),
  OVERDUE("overdue");

  private final String code;

  FeeStatus(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static FeeStatus fromCode(String code) {
    for (FeeStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown fee_status code: " + code);
  }
}
