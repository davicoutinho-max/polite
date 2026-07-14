package dev.civicpulse.identity.domain.model;

/** Mirrors {@code document_status_options} — outcome of one call through the
 * {@link dev.civicpulse.identity.application.port.out.DocumentVerificationGateway}
 * anti-corruption layer. */
public enum DocumentStatus {
  PENDING("pending"),
  VERIFIED("verified"),
  REJECTED("rejected");

  private final String code;

  DocumentStatus(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static DocumentStatus fromCode(String code) {
    for (DocumentStatus status : values()) {
      if (status.code.equals(code)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown document_status code: " + code);
  }
}
