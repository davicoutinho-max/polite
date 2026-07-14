package dev.civicpulse.legislative.domain.model;

/** Mirrors {@code committee_kind_options}. */
public enum CommitteeKind {
  COMMITTEE("committee"),
  FRONT("front"),
  CPI("cpi");

  private final String code;

  CommitteeKind(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static CommitteeKind fromCode(String code) {
    for (CommitteeKind kind : values()) {
      if (kind.code.equals(code)) {
        return kind;
      }
    }
    throw new IllegalArgumentException("Unknown committee_kind code: " + code);
  }
}
