package dev.civicpulse.identity.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentNumberValidatorTest {

  @Test
  void acceptsARealCpf() {
    assertThat(DocumentNumberValidator.isValid(DocumentType.CPF, "52998224725")).isTrue();
  }

  @Test
  void rejectsACpfWithWrongCheckDigits() {
    assertThat(DocumentNumberValidator.isValid(DocumentType.CPF, "52998224700")).isFalse();
  }

  @Test
  void rejectsARepeatedDigitCpf() {
    assertThat(DocumentNumberValidator.isValid(DocumentType.CPF, "11111111111")).isFalse();
  }

  @Test
  void acceptsARealCnpj() {
    assertThat(DocumentNumberValidator.isValid(DocumentType.CNPJ, "11444777000161")).isTrue();
  }

  @Test
  void rejectsACnpjWithWrongCheckDigits() {
    assertThat(DocumentNumberValidator.isValid(DocumentType.CNPJ, "11444777000100")).isFalse();
  }
}
