package dev.civicpulse.fundraising.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.civicpulse.fundraising.application.port.out.ContributionRepository;
import dev.civicpulse.fundraising.application.port.out.FundraiserRepository;
import dev.civicpulse.fundraising.domain.model.Contribution;
import dev.civicpulse.fundraising.domain.model.Fundraiser;
import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Exercises the real JPA/Postgres adapters against the shared local-dev database (see
 * identity-service's equivalent test for the rationale on why this isn't Testcontainers). */
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:postgresql://localhost:5432/fundraising_service",
      "spring.datasource.username=fundraising_service_app",
      "spring.datasource.password=fundraising_dev_pw"
    })
class FundraisingRepositoryIntegrationTest {

  @BeforeAll
  static void requireLocalPostgres() {
    boolean reachable;
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", 5432), 500);
      reachable = true;
    } catch (Exception e) {
      reachable = false;
    }
    assumeTrue(reachable, "Shared dev Postgres (localhost:5432) is not running — start it with "
        + "'docker compose up -d postgres' in backends/ to run this test");
  }

  @Autowired private FundraiserRepository fundraiserRepository;
  @Autowired private ContributionRepository contributionRepository;

  @Test
  void savesAndRetrievesFundraiser() {
    UUID id = UUID.randomUUID();
    Fundraiser fundraiser = Fundraiser.create(id, UUID.randomUUID(), "Help rebuild", "desc", FundraiserCategory.SOCIAL, 100_000, null, true, Instant.now());

    fundraiserRepository.save(fundraiser);

    assertThat(fundraiserRepository.findById(id)).isPresent().get().satisfies(found -> assertThat(found.title()).isEqualTo("Help rebuild"));
  }

  @Test
  void findByCategoryFiltersCorrectly() {
    UUID socialId = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    fundraiserRepository.save(Fundraiser.create(socialId, UUID.randomUUID(), "social one", null, FundraiserCategory.SOCIAL, 1000, null, true, Instant.now()));
    fundraiserRepository.save(Fundraiser.create(partyId, UUID.randomUUID(), "party one", null, FundraiserCategory.PARTY, 1000, null, true, Instant.now()));

    assertThat(fundraiserRepository.findByCategory(FundraiserCategory.PARTY, 0, 50)).extracting(Fundraiser::id).contains(partyId).doesNotContain(socialId);
  }

  @Test
  void contributionRoundTripAndUniquePaymentIntentCheck() {
    UUID fundraiserId = UUID.randomUUID();
    fundraiserRepository.save(Fundraiser.create(fundraiserId, UUID.randomUUID(), "title", null, FundraiserCategory.SOCIAL, 100_000, null, true, Instant.now()));
    UUID paymentIntentId = UUID.randomUUID();

    assertThat(contributionRepository.existsByPaymentIntentId(paymentIntentId)).isFalse();

    contributionRepository.save(Contribution.record(fundraiserId, UUID.randomUUID(), 5000, paymentIntentId, Instant.now()));

    assertThat(contributionRepository.existsByPaymentIntentId(paymentIntentId)).isTrue();
    assertThat(contributionRepository.findByFundraiserId(fundraiserId)).anySatisfy(c -> assertThat(c.amountCents()).isEqualTo(5000));
  }
}
