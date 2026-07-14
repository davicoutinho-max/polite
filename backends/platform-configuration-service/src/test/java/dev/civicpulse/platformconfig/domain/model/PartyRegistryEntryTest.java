package dev.civicpulse.platformconfig.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PartyRegistryEntryTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void registerStartsWithZeroMembers() {
    PartyRegistryEntry entry = PartyRegistryEntry.register(UUID.randomUUID(), "Progressive Party", "PROG", 13, "Jane Doe", "Progressivism", NOW);

    assertThat(entry.memberCount()).isZero();
    assertThat(entry.acronym()).isEqualTo("PROG");
  }

  @Test
  void blankAcronymIsRejected() {
    assertThatThrownBy(() -> PartyRegistryEntry.register(UUID.randomUUID(), "Progressive Party", " ", 13, null, null, NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("acronym");
  }
}
