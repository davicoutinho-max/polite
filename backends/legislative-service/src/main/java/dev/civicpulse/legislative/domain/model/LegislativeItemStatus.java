package dev.civicpulse.legislative.domain.model;

/** Mirrors {@code legislative_item_status_options}. Forward-only filed -> in_committee ->
 * floor_vote -> passed, except the one-way exit to rejected from any earlier status. */
public enum LegislativeItemStatus {
  FILED("filed", 1),
  IN_COMMITTEE("in_committee", 2),
  FLOOR_VOTE("floor_vote", 3),
  PASSED("passed", 4),
  REJECTED("rejected", 5);

  private final String code;
  private final int sortOrder;

  LegislativeItemStatus(String code, int sortOrder) {
    this.code = code;
    this.sortOrder = sortOrder;
  }

  public String code() {
    return code;
  }

  public int sortOrder() {
    return sortOrder;
  }

  public static LegislativeItemStatus fromCode(String code) {
    for (LegislativeItemStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown legislative_item_status code: " + code);
  }
}
