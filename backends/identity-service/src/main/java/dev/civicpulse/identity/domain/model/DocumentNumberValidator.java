package dev.civicpulse.identity.domain.model;

/** Standard Brazilian CPF/CNPJ check-digit algorithms (mod 11) — format validation only, no
 * lookup against a real Receita Federal registry (there is no such integration anywhere in this
 * system). Mirrors the frontend's {@code br-documents.ts} exactly (see {@code isValidCpf}/{@code
 * isValidCnpj} there) — this is the server-side enforcement of the same rule, since the frontend
 * check alone never protects a direct API caller. */
public final class DocumentNumberValidator {

  private DocumentNumberValidator() {}

  public static boolean isValid(DocumentType type, String digitsOnly) {
    return switch (type) {
      case CPF -> isValidCpf(digitsOnly);
      case CNPJ -> isValidCnpj(digitsOnly);
    };
  }

  private static boolean isValidCpf(String cpf) {
    if (cpf.length() != 11 || allDigitsSame(cpf)) {
      return false;
    }
    int d1 = checkDigit(cpf, 9, new int[] {10, 9, 8, 7, 6, 5, 4, 3, 2});
    int d2 = checkDigit(cpf, 10, new int[] {11, 10, 9, 8, 7, 6, 5, 4, 3, 2});
    return cpf.charAt(9) - '0' == d1 && cpf.charAt(10) - '0' == d2;
  }

  private static boolean isValidCnpj(String cnpj) {
    if (cnpj.length() != 14 || allDigitsSame(cnpj)) {
      return false;
    }
    int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    int d1 = checkDigit(cnpj, 12, weights1);
    int d2 = checkDigit(cnpj, 13, weights2);
    return cnpj.charAt(12) - '0' == d1 && cnpj.charAt(13) - '0' == d2;
  }

  private static int checkDigit(String digits, int length, int[] weights) {
    int sum = 0;
    for (int i = 0; i < length; i++) {
      sum += (digits.charAt(i) - '0') * weights[i];
    }
    int remainder = sum % 11;
    return remainder < 2 ? 0 : 11 - remainder;
  }

  private static boolean allDigitsSame(String digits) {
    return digits.chars().distinct().count() == 1;
  }
}
