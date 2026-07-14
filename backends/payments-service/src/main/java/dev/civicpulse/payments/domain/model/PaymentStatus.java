package dev.civicpulse.payments.domain.model;

/** Mirrors {@code payment_status_options}. */
public enum PaymentStatus {
  CREATED("created"),
  AUTHORIZED("authorized"),
  CAPTURED("captured"),
  FAILED("failed"),
  REFUNDED("refunded"),
  CANCELED("canceled");

  private final String code;

  PaymentStatus(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static PaymentStatus fromCode(String code) {
    for (PaymentStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown payment_status code: " + code);
  }
}
