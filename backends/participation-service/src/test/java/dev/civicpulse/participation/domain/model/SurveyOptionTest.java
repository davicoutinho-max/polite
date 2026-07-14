package dev.civicpulse.participation.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SurveyOptionTest {

  @Test
  void createStartsAtZeroVotes() {
    SurveyOption option = SurveyOption.create(UUID.randomUUID(), UUID.randomUUID(), "Yes");

    assertThat(option.votesCount()).isZero();
  }

  @Test
  void incrementVotesIncreasesCount() {
    SurveyOption option = SurveyOption.create(UUID.randomUUID(), UUID.randomUUID(), "Yes");

    option.incrementVotes();
    option.incrementVotes();

    assertThat(option.votesCount()).isEqualTo(2);
  }
}
