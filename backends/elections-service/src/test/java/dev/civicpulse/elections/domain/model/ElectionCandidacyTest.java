package dev.civicpulse.elections.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ElectionCandidacyTest {

  @Test
  void nominateKeepsFields() {
    UUID electionId = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();

    ElectionCandidacy candidacy = ElectionCandidacy.nominate(electionId, politicianId);

    assertThat(candidacy.electionId()).isEqualTo(electionId);
    assertThat(candidacy.politicianAccountId()).isEqualTo(politicianId);
  }

  @Test
  void equalityIsBasedOnBothIds() {
    UUID electionId = UUID.randomUUID();
    UUID politicianId = UUID.randomUUID();

    ElectionCandidacy a = ElectionCandidacy.nominate(electionId, politicianId);
    ElectionCandidacy b = ElectionCandidacy.nominate(electionId, politicianId);

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }
}
