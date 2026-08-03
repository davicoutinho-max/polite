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

  /** Self-registration only ever collects a single "document number" field (no explicit type
   * picker) and infers CPF vs. CNPJ purely from digit count — 11 for an individual, 14 for a
   * party/committee entity. */
  public static DocumentType fromDigitCount(int digitCount) {
    for (DocumentType type : values()) {
      if (type.digitCount == digitCount) {
        return type;
      }
    }
    throw new IllegalArgumentException("documentNumber must have 11 (CPF) or 14 (CNPJ) digits");
  }
}
