package dev.civicpulse.privacycompliance.domain.model;

/** Mirrors {@code export_status_options}. Forward-only pending -> processing -> ready, except
 * the one-way exit to {@code FAILED} from either pending or processing. */
public enum ExportStatus {
  PENDING("pending", 1),
  PROCESSING("processing", 2),
  READY("ready", 3),
  FAILED("failed", 4);

  private final String code;
  private final int sortOrder;

  ExportStatus(String code, int sortOrder) {
    this.code = code;
    this.sortOrder = sortOrder;
  }

  public String code() {
    return code;
  }

  public int sortOrder() {
    return sortOrder;
  }

  public static ExportStatus fromCode(String code) {
    for (ExportStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown export_status code: " + code);
  }
}
