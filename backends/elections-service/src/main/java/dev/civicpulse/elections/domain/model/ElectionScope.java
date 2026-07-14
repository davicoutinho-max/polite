package dev.civicpulse.elections.domain.model;

/** Mirrors {@code election_scope_options}. */
public enum ElectionScope {
  NACIONAL("nacional"),
  ESTADUAL("estadual"),
  MUNICIPAL("municipal");

  private final String code;

  ElectionScope(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static ElectionScope fromCode(String code) {
    for (ElectionScope scope : values()) {
      if (scope.code.equals(code)) {
        return scope;
      }
    }
    throw new IllegalArgumentException("Unknown election_scope code: " + code);
  }
}
