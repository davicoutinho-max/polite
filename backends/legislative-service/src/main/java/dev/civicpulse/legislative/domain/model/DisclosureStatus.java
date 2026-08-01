package dev.civicpulse.legislative.domain.model;

/** Mirrors {@code accountability_status_options}. Each submission is scored independently by the
 * AI reviewer — unlike most status enums in this codebase, there's no forward-only transition: a
 * rejected submission just sits there as history while the politician tries again with a new
 * submission (see AccountabilityDisclosure's javadoc). */
public enum DisclosureStatus {
  APPROVED("approved"),
  REJECTED("rejected");

  private final String code;

  DisclosureStatus(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static DisclosureStatus fromCode(String code) {
    for (DisclosureStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown accountability_status code: " + code);
  }
}
