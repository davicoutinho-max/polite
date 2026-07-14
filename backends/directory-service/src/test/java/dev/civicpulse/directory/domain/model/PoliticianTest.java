package dev.civicpulse.directory.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PoliticianTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void projectCreatesUnlinkedPolitician() {
    Politician politician = Politician.project(UUID.randomUUID(), "Jane Doe", "janedoe", null, null, null, null, null, null, NOW);

    assertThat(politician.followersCount()).isZero();
    assertThat(politician.billsCount()).isZero();
    assertThat(politician.partyId()).isEmpty();
    assertThat(politician.verified()).isFalse();
  }

  @Test
  void reassignUpdatesPartyAndOfficeWithoutTouchingFollowers() {
    Politician politician = Politician.project(UUID.randomUUID(), "Jane Doe", "janedoe", null, null, null, null, null, null, NOW);
    politician.incrementFollowers(NOW);

    UUID partyId = UUID.randomUUID();
    Instant later = NOW.plusSeconds(60);
    politician.reassign("Deputy", GovLevel.FEDERAL, partyId, "PROG", "SP", later);

    assertThat(politician.office()).contains("Deputy");
    assertThat(politician.level()).contains(GovLevel.FEDERAL);
    assertThat(politician.partyId()).contains(partyId);
    assertThat(politician.partyAcronym()).contains("PROG");
    assertThat(politician.state()).contains("SP");
    assertThat(politician.followersCount()).isEqualTo(1);
    assertThat(politician.updatedAt()).isEqualTo(later);
  }

  @Test
  void followersCountNeverGoesNegative() {
    Politician politician = Politician.project(UUID.randomUUID(), "Jane Doe", "janedoe", null, null, null, null, null, null, NOW);

    politician.decrementFollowers(NOW);

    assertThat(politician.followersCount()).isZero();
  }

  @Test
  void incrementThenDecrementReturnsToZero() {
    Politician politician = Politician.project(UUID.randomUUID(), "Jane Doe", "janedoe", null, null, null, null, null, null, NOW);

    politician.incrementFollowers(NOW);
    politician.incrementFollowers(NOW);
    politician.decrementFollowers(NOW);

    assertThat(politician.followersCount()).isEqualTo(1);
  }

  @Test
  void blankHandleIsRejected() {
    org.assertj.core.api.Assertions.assertThatThrownBy(
            () -> Politician.project(UUID.randomUUID(), "Jane Doe", "  ", null, null, null, null, null, null, NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("handle");
  }
}
