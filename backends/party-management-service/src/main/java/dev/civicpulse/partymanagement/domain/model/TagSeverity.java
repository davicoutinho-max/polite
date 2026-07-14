package dev.civicpulse.partymanagement.domain.model;

/** Mirrors {@code tag_severity_options} — the shared UI severity vocabulary, duplicated
 * locally here and in Feed & Content (see schema.sql's comment on this table). */
public enum TagSeverity {
  SUCCESS("success"),
  WARNING("warning"),
  DANGER("danger"),
  INFO("info"),
  NEUTRAL("neutral"),
  SECONDARY("secondary"),
  PRIMARY("primary");

  private final String code;

  TagSeverity(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static TagSeverity fromCode(String code) {
    for (TagSeverity severity : values()) {
      if (severity.code.equals(code)) {
        return severity;
      }
    }
    throw new IllegalArgumentException("Unknown tag_severity code: " + code);
  }
}
