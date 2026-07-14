package dev.civicpulse.identity.domain.model;

/** Mirrors {@code document_type_options}. CPF identifies an individual (citizen or politician);
 * CNPJ identifies the party/committee entity behind a party account. */
public enum DocumentType {
  CPF("cpf", 11),
  CNPJ("cnpj", 14);

  private final String code;
  private final int digitCount;

  DocumentType(String code, int digitCount) {
    this.code = code;
    this.digitCount = digitCount;
  }

  public String code() {
    return code;
  }

  public int digitCount() {
    return digitCount;
  }

  public static DocumentType fromCode(String code) {
    for (DocumentType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown document_type code: " + code);
  }
}
