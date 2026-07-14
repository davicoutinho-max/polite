package dev.civicpulse.legislative.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AttendanceRecordTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void presenceRateIsZeroWhenNoRecordsYet() {
    AttendanceRecord record = AttendanceRecord.initialize(UUID.randomUUID(), NOW);

    assertThat(record.presenceRate()).isZero();
  }

  @Test
  void presenceRateIsComputedFromPresentAndAbsentCounts() {
    AttendanceRecord record = AttendanceRecord.initialize(UUID.randomUUID(), NOW);

    record.recordPresence(true, NOW);
    record.recordPresence(true, NOW);
    record.recordPresence(true, NOW);
    record.recordPresence(false, NOW);

    assertThat(record.present()).isEqualTo(3);
    assertThat(record.absent()).isEqualTo(1);
    assertThat(record.presenceRate()).isCloseTo(75.0, within(0.001));
  }
}
