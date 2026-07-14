package dev.civicpulse.payments.domain.model;

/** Mirrors {@code ledger_direction_options}. */
public enum LedgerDirection {
  DEBIT("debit"),
  CREDIT("credit");

  private final String code;

  LedgerDirection(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static LedgerDirection fromCode(String code) {
    for (LedgerDirection direction : values()) {
      if (direction.code.equals(code)) {
        return direction;
      }
    }
    throw new IllegalArgumentException("Unknown ledger_direction code: " + code);
  }
}
