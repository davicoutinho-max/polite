package dev.civicpulse.privacycompliance.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConsentRecordTest {

  @Test
  void updateChangesGrantedAndTimestamp() {
    Instant t0 = Instant.parse("2026-01-01T00:00:00Z");
    Instant t1 = Instant.parse("2026-01-01T01:00:00Z");
    ConsentRecord record = ConsentRecord.record(UUID.randomUUID(), ConsentPurpose.MARKETING, true, t0);

    record.update(false, t1);

    assertThat(record.granted()).isFalse();
    assertThat(record.updatedAt()).isEqualTo(t1);
  }
}
