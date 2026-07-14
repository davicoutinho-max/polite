package dev.civicpulse.elections.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ElectionTest {

  @Test
  void createKeepsFieldsAsProvided() {
    UUID id = UUID.randomUUID();
    LocalDate date = LocalDate.of(2026, 10, 4);

    Election election = Election.create(id, "Eleicoes Municipais 2026", ElectionScope.MUNICIPAL, date, "desc");

    assertThat(election.id()).isEqualTo(id);
    assertThat(election.title()).isEqualTo("Eleicoes Municipais 2026");
    assertThat(election.scope()).isEqualTo(ElectionScope.MUNICIPAL);
    assertThat(election.electionDate()).isEqualTo(date);
    assertThat(election.description()).contains("desc");
  }

  @Test
  void createRejectsBlankTitle() {
    assertThatThrownBy(() -> Election.create(UUID.randomUUID(), " ", ElectionScope.NACIONAL, LocalDate.now(), null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void equalityIsBasedOnId() {
    UUID id = UUID.randomUUID();
    Election a = Election.create(id, "a", ElectionScope.NACIONAL, LocalDate.now(), null);
    Election b = Election.reconstitute(id, "b", ElectionScope.ESTADUAL, LocalDate.now().plusDays(1), "x");

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }
}
