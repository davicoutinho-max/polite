package dev.civicpulse.membershipaffiliation.domain.model;

/** Mirrors {@code affiliation_status_options}. Ordinal position (via {@link #sortOrder()})
 * defines the forward-only happy path; {@code REJECTED} is reachable from any non-terminal
 * status (see affiliation-lifecycle.bpmn) and is the one backward-looking exception. */
public enum AffiliationStatus {
  NOT_STARTED("not_started", 1),
  REQUESTED("requested", 2),
  UNDER_REVIEW("under_review", 3),
  PARTY_APPROVED("party_approved", 4),
  ELECTORAL_JUSTICE("electoral_justice", 5),
  AFFILIATED("affiliated", 6),
  REJECTED("rejected", 7);

  private final String code;
  private final int sortOrder;

  AffiliationStatus(String code, int sortOrder) {
    this.code = code;
    this.sortOrder = sortOrder;
  }

  public String code() {
    return code;
  }

  public int sortOrder() {
    return sortOrder;
  }

  public static AffiliationStatus fromCode(String code) {
    for (AffiliationStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown affiliation_status code: " + code);
  }
}
