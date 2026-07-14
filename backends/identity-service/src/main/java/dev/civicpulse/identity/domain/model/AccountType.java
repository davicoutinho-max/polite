package dev.civicpulse.identity.domain.model;

/** Mirrors the {@code account_type_options} parameter table — a fixed, small vocabulary that
 * still lives as data in Postgres so new codes never require a redeploy. This enum exists
 * because the domain logic in this service only ever needs to reason about these four; it is
 * validated against the parameter table at the persistence boundary, not hardcoded there. */
public enum AccountType {
  CITIZEN("citizen"),
  POLITICIAN("politician"),
  PARTY("party"),
  ADMIN("admin");

  private final String code;

  AccountType(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static AccountType fromCode(String code) {
    for (AccountType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown account_type code: " + code);
  }
}
