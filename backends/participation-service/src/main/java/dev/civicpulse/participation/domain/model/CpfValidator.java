package dev.civicpulse.participation.domain.model;

/** Standard Brazilian CPF check-digit algorithm (mod 11) — format validation only, no lookup
 * against a real Receita Federal registry (there is no such integration anywhere in this
 * system). */
public final class CpfValidator {

  private CpfValidator() {}

  public static boolean isValid(String rawCpf) {
    if (rawCpf == null) {
      return false;
    }
    String cpf = rawCpf.replaceAll("\\D", "");
    if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
      return false;
    }
    int firstDigit = checkDigit(cpf.substring(0, 9), 10);
    int secondDigit = checkDigit(cpf.substring(0, 9) + firstDigit, 11);
    return cpf.equals(cpf.substring(0, 9) + firstDigit + secondDigit);
  }

  private static int checkDigit(String base, int startWeight) {
    int sum = 0;
    int weight = startWeight;
    for (int i = 0; i < base.length(); i++) {
      sum += (base.charAt(i) - '0') * weight--;
    }
    int remainder = sum % 11;
    return remainder < 2 ? 0 : 11 - remainder;
  }
}
