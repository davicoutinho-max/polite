package dev.civicpulse.analytics.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class KpiSummaryTest {

  @Test
  void engagementRateIsZeroWhenReachIsZero() {
    KpiSummary summary = KpiSummary.of(0, 0, 0, 0, 0);
    assertThat(summary.engagementRatePercent()).isZero();
  }

  @Test
  void engagementRateIsComputedFromLikesPlusCommentsOverReach() {
    KpiSummary summary = KpiSummary.of(5, 30, 10, 2, 20);
    assertThat(summary.engagementRatePercent()).isCloseTo(200.0, within(0.001));
  }
}
