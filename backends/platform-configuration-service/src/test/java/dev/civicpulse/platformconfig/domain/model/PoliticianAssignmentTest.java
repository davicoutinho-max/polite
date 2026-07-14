package dev.civicpulse.platformconfig.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PoliticianAssignmentTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void reassignUpdatesPartyAndTimestamp() {
    UUID politicianId = UUID.randomUUID();
    UUID firstParty = UUID.randomUUID();
    UUID secondParty = UUID.randomUUID();
    PoliticianAssignment assignment = PoliticianAssignment.assign(politicianId, firstParty, NOW);

    Instant later = NOW.plusSeconds(60);
    assignment.reassign(secondParty, later);

    assertThat(assignment.partyId()).isEqualTo(secondParty);
    assertThat(assignment.updatedAt()).isEqualTo(later);
  }
}
