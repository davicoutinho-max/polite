package dev.civicpulse.directory.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PartyTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void projectStartsWithZeroMembers() {
    Party party = Party.project(UUID.randomUUID(), "Progressive Party", "PROG", 13, "Progressivism", PartySpectrum.CENTER_LEFT, 1990, "Jane Doe", null, NOW);

    assertThat(party.memberCount()).isZero();
  }

  @Test
  void memberCountNeverGoesNegative() {
    Party party = Party.project(UUID.randomUUID(), "Progressive Party", "PROG", 13, null, null, null, null, null, NOW);

    party.decrementMembers(NOW);

    assertThat(party.memberCount()).isZero();
  }

  @Test
  void incrementAndDecrementMembers() {
    Party party = Party.project(UUID.randomUUID(), "Progressive Party", "PROG", 13, null, null, null, null, null, NOW);

    party.incrementMembers(NOW);
    party.incrementMembers(NOW);
    party.decrementMembers(NOW);

    assertThat(party.memberCount()).isEqualTo(1);
  }
}
