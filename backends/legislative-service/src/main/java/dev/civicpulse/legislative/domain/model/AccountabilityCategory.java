package dev.civicpulse.legislative.domain.model;

/** Mirrors {@code accountability_category_options} — the real categories of public money a
 * Brazilian federal politician is accountable for (office budget, the CEAP parliamentary quota,
 * parliamentary amendments, travel, and institutional advertising). */
public enum AccountabilityCategory {
  OFFICE_BUDGET("office_budget"),
  PARLIAMENTARY_QUOTA("parliamentary_quota"),
  PARLIAMENTARY_AMENDMENTS("parliamentary_amendments"),
  TRAVEL_ALLOWANCE("travel_allowance"),
  ADVERTISING("advertising");

  private final String code;

  AccountabilityCategory(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static AccountabilityCategory fromCode(String code) {
    for (AccountabilityCategory category : values()) {
      if (category.code.equals(code)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unknown accountability_category code: " + code);
  }
}
