package dev.civicpulse.legislative.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExpenseLineTest {

  @Test
  void shareIsZeroWhenTotalIsZero() {
    ExpenseLine line = ExpenseLine.record(UUID.randomUUID(), "Travel", 5_000L);

    assertThat(line.shareOf(0L)).isZero();
  }

  @Test
  void shareIsComputedFromAmountOverTotal() {
    ExpenseLine line = ExpenseLine.record(UUID.randomUUID(), "Travel", 2_500L);

    assertThat(line.shareOf(10_000L)).isCloseTo(25.0, within(0.001));
  }
}
