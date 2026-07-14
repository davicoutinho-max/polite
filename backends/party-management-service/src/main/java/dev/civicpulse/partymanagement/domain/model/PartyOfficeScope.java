package dev.civicpulse.partymanagement.domain.model;

/** Mirrors {@code party_office_scope_options}. */
public enum PartyOfficeScope {
  NACIONAL("nacional"),
  ESTADUAL("estadual"),
  MUNICIPAL("municipal");

  private final String code;

  PartyOfficeScope(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static PartyOfficeScope fromCode(String code) {
    for (PartyOfficeScope scope : values()) {
      if (scope.code.equals(code)) {
        return scope;
      }
    }
    throw new IllegalArgumentException("Unknown party_office_scope code: " + code);
  }
}
