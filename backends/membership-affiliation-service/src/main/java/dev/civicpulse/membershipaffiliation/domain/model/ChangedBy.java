package dev.civicpulse.membershipaffiliation.domain.model;

/** Mirrors {@code changed_by_options} — who/what triggered a status transition. */
public enum ChangedBy {
  CITIZEN("citizen"),
  PARTY("party"),
  ELECTORAL_JUSTICE("electoral_justice"),
  SYSTEM("system");

  private final String code;

  ChangedBy(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static ChangedBy fromCode(String code) {
    for (ChangedBy value : values()) {
      if (value.code.equals(code)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Unknown changed_by code: " + code);
  }
}
