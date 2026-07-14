package dev.civicpulse.fundraising.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FundraiserTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void createStartsAtZeroRaisedAndZeroSupporters() {
    Fundraiser fundraiser =
        Fundraiser.create(UUID.randomUUID(), UUID.randomUUID(), "Help rebuild the school", "desc", FundraiserCategory.SOCIAL, 100_000, null, true, NOW);

    assertThat(fundraiser.raisedCents()).isZero();
    assertThat(fundraiser.supportersCount()).isZero();
    assertThat(fundraiser.goalCents()).isEqualTo(100_000);
  }

  @Test
  void createRejectsNonPositiveGoal() {
    assertThatThrownBy(
            () -> Fundraiser.create(UUID.randomUUID(), UUID.randomUUID(), "title", null, FundraiserCategory.SOCIAL, 0, null, true, NOW))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void recordContributionIncreasesRaisedAndSupporters() {
    Fundraiser fundraiser = Fundraiser.create(UUID.randomUUID(), UUID.randomUUID(), "title", null, FundraiserCategory.SOCIAL, 100_000, null, true, NOW);

    fundraiser.recordContribution(30_000);

    assertThat(fundraiser.raisedCents()).isEqualTo(30_000);
    assertThat(fundraiser.supportersCount()).isEqualTo(1);
  }

  @Test
  void recordContributionReturnsTrueOnlyWhenCrossingGoal() {
    Fundraiser fundraiser = Fundraiser.create(UUID.randomUUID(), UUID.randomUUID(), "title", null, FundraiserCategory.SOCIAL, 100_000, null, true, NOW);

    assertThat(fundraiser.recordContribution(60_000)).isFalse();
    assertThat(fundraiser.recordContribution(30_000)).isFalse();
    assertThat(fundraiser.recordContribution(20_000)).isTrue();
    assertThat(fundraiser.recordContribution(10_000)).isFalse();
  }

  @Test
  void recordContributionRejectsNonPositiveAmount() {
    Fundraiser fundraiser = Fundraiser.create(UUID.randomUUID(), UUID.randomUUID(), "title", null, FundraiserCategory.SOCIAL, 100_000, null, true, NOW);

    assertThatThrownBy(() -> fundraiser.recordContribution(0)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void equalityIsBasedOnId() {
    UUID id = UUID.randomUUID();
    Fundraiser a = Fundraiser.create(id, UUID.randomUUID(), "a", null, FundraiserCategory.SOCIAL, 1000, null, true, NOW);
    Fundraiser b = Fundraiser.reconstitute(id, UUID.randomUUID(), "b", null, FundraiserCategory.PARTY, 2000, 500, 3, null, false, NOW);

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }
}
