package dev.civicpulse.fundraising.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ContributionTest {

  @Test
  void recordCreatesContributionWithGivenFields() {
    UUID fundraiserId = UUID.randomUUID();
    UUID supporterId = UUID.randomUUID();
    UUID paymentIntentId = UUID.randomUUID();
    Instant now = Instant.parse("2026-01-01T00:00:00Z");

    Contribution contribution = Contribution.record(fundraiserId, supporterId, 5000, paymentIntentId, now);

    assertThat(contribution.fundraiserId()).isEqualTo(fundraiserId);
    assertThat(contribution.supporterAccountId()).isEqualTo(supporterId);
    assertThat(contribution.amountCents()).isEqualTo(5000);
    assertThat(contribution.paymentIntentId()).isEqualTo(paymentIntentId);
  }

  @Test
  void recordRejectsNonPositiveAmount() {
    assertThatThrownBy(() -> Contribution.record(UUID.randomUUID(), UUID.randomUUID(), 0, UUID.randomUUID(), Instant.now()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
