package dev.civicpulse.payments.domain.model;

/** Mirrors {@code payment_purpose_options}. */
public enum PaymentPurpose {
  MEMBERSHIP_FEE("membership_fee"),
  FUNDRAISING_CONTRIBUTION("fundraising_contribution");

  private final String code;

  PaymentPurpose(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static PaymentPurpose fromCode(String code) {
    for (PaymentPurpose purpose : values()) {
      if (purpose.code.equals(code)) {
        return purpose;
      }
    }
    throw new IllegalArgumentException("Unknown payment_purpose code: " + code);
  }
}
