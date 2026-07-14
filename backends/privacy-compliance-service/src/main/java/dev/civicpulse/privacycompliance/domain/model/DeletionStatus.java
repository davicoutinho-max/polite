package dev.civicpulse.privacycompliance.domain.model;

/** Mirrors {@code deletion_status_options}. Forward-only pending -> confirmed -> processing ->
 * completed, except the one-way exit to {@code CANCELED} from pending or confirmed only (once
 * processing has started, the erasure saga can no longer be canceled). */
public enum DeletionStatus {
  PENDING("pending", 1),
  CONFIRMED("confirmed", 2),
  PROCESSING("processing", 3),
  COMPLETED("completed", 4),
  CANCELED("canceled", 5);

  private final String code;
  private final int sortOrder;

  DeletionStatus(String code, int sortOrder) {
    this.code = code;
    this.sortOrder = sortOrder;
  }

  public String code() {
    return code;
  }

  public int sortOrder() {
    return sortOrder;
  }

  public static DeletionStatus fromCode(String code) {
    for (DeletionStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown deletion_status code: " + code);
  }
}
