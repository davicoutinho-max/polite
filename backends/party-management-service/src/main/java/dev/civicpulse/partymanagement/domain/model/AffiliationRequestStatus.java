package dev.civicpulse.partymanagement.domain.model;

/** Mirrors {@code affiliation_request_status_options}. */
public enum AffiliationRequestStatus {
  PENDING("pending"),
  APPROVED("approved"),
  REJECTED("rejected");

  private final String code;

  AffiliationRequestStatus(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static AffiliationRequestStatus fromCode(String code) {
    for (AffiliationRequestStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown affiliation_request_status code: " + code);
  }
}
