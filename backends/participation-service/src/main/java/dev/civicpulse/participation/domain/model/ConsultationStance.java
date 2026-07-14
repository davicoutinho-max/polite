package dev.civicpulse.participation.domain.model;

/** Mirrors {@code consultation_stance_options}. */
public enum ConsultationStance {
  FAVOR("favor"),
  AGAINST("against"),
  NEUTRAL("neutral");

  private final String code;

  ConsultationStance(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static ConsultationStance fromCode(String code) {
    for (ConsultationStance stance : values()) {
      if (stance.code.equals(code)) {
        return stance;
      }
    }
    throw new IllegalArgumentException("Unknown consultation_stance code: " + code);
  }
}
