package dev.civicpulse.membershipaffiliation.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.civicpulse.membershipaffiliation.domain.exception.InvalidAffiliationTransitionException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AffiliationTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void requestStartsInRequestedStatus() {
    Affiliation affiliation = Affiliation.request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW);

    assertThat(affiliation.status()).isEqualTo(AffiliationStatus.REQUESTED);
    assertThat(affiliation.requestedAt()).contains(NOW);
  }

  @Test
  void happyPathAdvancesThroughEveryStatusInOrder() {
    Affiliation affiliation = Affiliation.request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW);

    affiliation.startReview(NOW);
    assertThat(affiliation.status()).isEqualTo(AffiliationStatus.UNDER_REVIEW);

    affiliation.approveByParty(NOW);
    assertThat(affiliation.status()).isEqualTo(AffiliationStatus.PARTY_APPROVED);

    affiliation.sendToElectoralJustice(NOW);
    assertThat(affiliation.status()).isEqualTo(AffiliationStatus.ELECTORAL_JUSTICE);

    affiliation.confirm(NOW);
    assertThat(affiliation.status()).isEqualTo(AffiliationStatus.AFFILIATED);
  }

  @Test
  void cannotSkipAStatus() {
    Affiliation affiliation = Affiliation.request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW);

    assertThatThrownBy(() -> affiliation.approveByParty(NOW)).isInstanceOf(InvalidAffiliationTransitionException.class);
  }

  @Test
  void cannotAdvancePastAffiliated() {
    Affiliation affiliation = Affiliation.request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW);
    affiliation.startReview(NOW);
    affiliation.approveByParty(NOW);
    affiliation.sendToElectoralJustice(NOW);
    affiliation.confirm(NOW);

    assertThatThrownBy(() -> affiliation.confirm(NOW)).isInstanceOf(InvalidAffiliationTransitionException.class);
  }

  @Test
  void rejectIsReachableFromAnyNonTerminalStatus() {
    Affiliation affiliation = Affiliation.request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW);

    affiliation.reject(NOW);

    assertThat(affiliation.status()).isEqualTo(AffiliationStatus.REJECTED);
  }

  @Test
  void cannotRejectAnAlreadyAffiliatedRecord() {
    Affiliation affiliation = Affiliation.request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW);
    affiliation.startReview(NOW);
    affiliation.approveByParty(NOW);
    affiliation.sendToElectoralJustice(NOW);
    affiliation.confirm(NOW);

    assertThatThrownBy(() -> affiliation.reject(NOW)).isInstanceOf(InvalidAffiliationTransitionException.class);
  }

  @Test
  void cannotRejectAnAlreadyRejectedRecord() {
    Affiliation affiliation = Affiliation.request(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW);
    affiliation.reject(NOW);

    assertThatThrownBy(() -> affiliation.reject(NOW)).isInstanceOf(InvalidAffiliationTransitionException.class);
  }
}
