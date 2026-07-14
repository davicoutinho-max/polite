package dev.civicpulse.payments.domain.model;

/** Mirrors {@code payment_gateway_options} — also reused as {@code payment_methods.type}
 * (a saved payment method IS a gateway-specific token). */
public enum PaymentGatewayType {
  PIX("pix"),
  CARD("card"),
  BOLETO("boleto");

  private final String code;

  PaymentGatewayType(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static PaymentGatewayType fromCode(String code) {
    for (PaymentGatewayType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown payment_gateway code: " + code);
  }
}
