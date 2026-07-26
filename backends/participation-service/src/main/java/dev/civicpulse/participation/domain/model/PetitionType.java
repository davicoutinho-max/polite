package dev.civicpulse.participation.domain.model;

/** Two legally distinct kinds of support a petition can collect:
 * VERIFIED_SUPPORT ("Apoio Verificado") — a lightweight identified-citizen endorsement (name,
 * CPF, birth date, city, state, confirmed via an SMS/email code); POPULAR_INITIATIVE ("Iniciativa
 * Popular") — aimed at petitions intending to meet the legal requirements for a formal
 * presentation, which additionally captures electoral data, requires an explicit electronic-
 * signature consent and an identity-validation step. */
public enum PetitionType {
  VERIFIED_SUPPORT,
  POPULAR_INITIATIVE;

  public String code() {
    return name().toLowerCase();
  }

  public static PetitionType fromCode(String code) {
    return valueOf(code.toUpperCase());
  }
}
