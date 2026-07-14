package dev.civicpulse.privacycompliance.domain.model;

/** Mirrors {@code consent_purpose_options}. */
public enum ConsentPurpose {
  ESSENTIAL("essential"),
  ANALYTICS("analytics"),
  PERSONALIZATION("personalization"),
  MARKETING("marketing");

  private final String code;

  ConsentPurpose(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static ConsentPurpose fromCode(String code) {
    for (ConsentPurpose purpose : values()) {
      if (purpose.code.equals(code)) {
        return purpose;
      }
    }
    throw new IllegalArgumentException("Unknown consent_purpose code: " + code);
  }
}
