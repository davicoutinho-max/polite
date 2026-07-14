package dev.civicpulse.participation.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConsultationResponseTest {

  @Test
  void changeStanceUpdatesStanceAndTimestamp() {
    Instant t0 = Instant.parse("2026-01-01T00:00:00Z");
    Instant t1 = Instant.parse("2026-01-01T01:00:00Z");
    ConsultationResponse response = ConsultationResponse.respond(UUID.randomUUID(), UUID.randomUUID(), ConsultationStance.FAVOR, t0);

    response.changeStance(ConsultationStance.AGAINST, t1);

    assertThat(response.stance()).isEqualTo(ConsultationStance.AGAINST);
    assertThat(response.updatedAt()).isEqualTo(t1);
  }
}
