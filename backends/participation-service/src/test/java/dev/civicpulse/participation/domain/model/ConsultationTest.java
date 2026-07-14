package dev.civicpulse.participation.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConsultationTest {

  @Test
  void createStartsAtZeroResponses() {
    Consultation consultation = Consultation.create(UUID.randomUUID(), "title", "desc", null);

    assertThat(consultation.responsesCount()).isZero();
  }

  @Test
  void recordNewResponseIncreasesCount() {
    Consultation consultation = Consultation.create(UUID.randomUUID(), "title", null, null);

    consultation.recordNewResponse();

    assertThat(consultation.responsesCount()).isEqualTo(1);
  }
}
