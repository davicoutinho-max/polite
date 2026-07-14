package dev.civicpulse.fundraising.domain.model;

/** Mirrors {@code fundraiser_category_options}. */
public enum FundraiserCategory {
  SOCIAL("social"),
  PARTY("party"),
  HUMANITARIAN("humanitarian");

  private final String code;

  FundraiserCategory(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static FundraiserCategory fromCode(String code) {
    for (FundraiserCategory category : values()) {
      if (category.code.equals(code)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unknown fundraiser_category code: " + code);
  }
}
