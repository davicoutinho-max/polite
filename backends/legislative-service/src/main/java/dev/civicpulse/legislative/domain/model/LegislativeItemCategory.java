package dev.civicpulse.legislative.domain.model;

/** Mirrors {@code legislative_item_category_options}. */
public enum LegislativeItemCategory {
  PROJECT("project"),
  PEC("pec"),
  CPI("cpi");

  private final String code;

  LegislativeItemCategory(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static LegislativeItemCategory fromCode(String code) {
    for (LegislativeItemCategory category : values()) {
      if (category.code.equals(code)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unknown legislative_item_category code: " + code);
  }
}
