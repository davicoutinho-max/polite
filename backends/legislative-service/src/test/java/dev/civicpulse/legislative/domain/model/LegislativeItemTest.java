package dev.civicpulse.legislative.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.civicpulse.legislative.domain.exception.InvalidStatusTransitionException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LegislativeItemTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void fileStartsAsFiled() {
    LegislativeItem item =
        LegislativeItem.file(UUID.randomUUID(), "PL 123/2026", "Title", "Summary", LegislativeItemCategory.PROJECT, LocalDate.now(), Set.of(), NOW);

    assertThat(item.status()).isEqualTo(LegislativeItemStatus.FILED);
  }

  @Test
  void advancesForwardOneStepAtATime() {
    LegislativeItem item = file();

    item.advanceStatus(LegislativeItemStatus.IN_COMMITTEE);
    assertThat(item.status()).isEqualTo(LegislativeItemStatus.IN_COMMITTEE);

    item.advanceStatus(LegislativeItemStatus.FLOOR_VOTE);
    assertThat(item.status()).isEqualTo(LegislativeItemStatus.FLOOR_VOTE);

    item.advanceStatus(LegislativeItemStatus.PASSED);
    assertThat(item.status()).isEqualTo(LegislativeItemStatus.PASSED);
  }

  @Test
  void cannotSkipAStatus() {
    LegislativeItem item = file();

    assertThatThrownBy(() -> item.advanceStatus(LegislativeItemStatus.FLOOR_VOTE)).isInstanceOf(InvalidStatusTransitionException.class);
  }

  @Test
  void canRejectFromAnyOpenStatus() {
    LegislativeItem item = file();
    item.advanceStatus(LegislativeItemStatus.IN_COMMITTEE);

    item.advanceStatus(LegislativeItemStatus.REJECTED);

    assertThat(item.status()).isEqualTo(LegislativeItemStatus.REJECTED);
  }

  @Test
  void cannotRejectAfterPassed() {
    LegislativeItem item = file();
    item.advanceStatus(LegislativeItemStatus.IN_COMMITTEE);
    item.advanceStatus(LegislativeItemStatus.FLOOR_VOTE);
    item.advanceStatus(LegislativeItemStatus.PASSED);

    assertThatThrownBy(() -> item.advanceStatus(LegislativeItemStatus.REJECTED)).isInstanceOf(InvalidStatusTransitionException.class);
  }

  private static LegislativeItem file() {
    return LegislativeItem.file(UUID.randomUUID(), "PL 123/2026", "Title", "Summary", LegislativeItemCategory.PROJECT, LocalDate.now(), Set.of(), NOW);
  }
}
