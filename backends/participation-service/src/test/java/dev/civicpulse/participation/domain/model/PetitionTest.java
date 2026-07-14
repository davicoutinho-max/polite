package dev.civicpulse.participation.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PetitionTest {

  @Test
  void createStartsAtZeroSignatures() {
    Petition petition = Petition.create(UUID.randomUUID(), "Save the park", "summary", "environment", 1000, null);

    assertThat(petition.signaturesCount()).isZero();
    assertThat(petition.goal()).isEqualTo(1000);
  }

  @Test
  void createRejectsNonPositiveGoal() {
    assertThatThrownBy(() -> Petition.create(UUID.randomUUID(), "title", null, null, 0, null)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void recordSignatureIncreasesCount() {
    Petition petition = Petition.create(UUID.randomUUID(), "title", null, null, 100, null);

    petition.recordSignature();
    petition.recordSignature();

    assertThat(petition.signaturesCount()).isEqualTo(2);
  }
}
