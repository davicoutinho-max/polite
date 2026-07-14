package dev.civicpulse.fundraising.application;

import dev.civicpulse.fundraising.application.port.in.ManageContributionUseCase;
import dev.civicpulse.fundraising.application.port.out.ContributionRepository;
import dev.civicpulse.fundraising.application.port.out.EventPublisher;
import dev.civicpulse.fundraising.application.port.out.FundraiserRepository;
import dev.civicpulse.fundraising.application.port.out.PaymentIntentLookupGateway;
import dev.civicpulse.fundraising.domain.event.ContributionReceived;
import dev.civicpulse.fundraising.domain.event.FundraiserGoalReached;
import dev.civicpulse.fundraising.domain.exception.FundraiserNotFoundException;
import dev.civicpulse.fundraising.domain.exception.LedgerNotPublicException;
import dev.civicpulse.fundraising.domain.model.Contribution;
import dev.civicpulse.fundraising.domain.model.Fundraiser;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContributionService implements ManageContributionUseCase {

  private final FundraiserRepository fundraiserRepository;
  private final ContributionRepository contributionRepository;
  private final PaymentIntentLookupGateway paymentIntentLookupGateway;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public ContributionService(
      FundraiserRepository fundraiserRepository,
      ContributionRepository contributionRepository,
      PaymentIntentLookupGateway paymentIntentLookupGateway,
      EventPublisher eventPublisher,
      Clock clock) {
    this.fundraiserRepository = fundraiserRepository;
    this.contributionRepository = contributionRepository;
    this.paymentIntentLookupGateway = paymentIntentLookupGateway;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void onPaymentCaptured(UUID fundraiserId, UUID paymentIntentId, long amountCents) {
    if (contributionRepository.existsByPaymentIntentId(paymentIntentId)) {
      return; // idempotent — reprocessed message
    }
    Fundraiser fundraiser = fundraiserRepository.findById(fundraiserId).orElseThrow(() -> new FundraiserNotFoundException(fundraiserId));

    var paymentIntent = paymentIntentLookupGateway.lookup(paymentIntentId);
    Instant now = clock.instant();
    Contribution contribution =
        contributionRepository.save(Contribution.record(fundraiserId, paymentIntent.payerAccountId(), amountCents, paymentIntentId, now));

    boolean goalJustReached = fundraiser.recordContribution(amountCents);
    fundraiserRepository.save(fundraiser);

    eventPublisher.publish(new ContributionReceived(fundraiserId, contribution.id(), paymentIntent.payerAccountId(), amountCents, now));
    if (goalJustReached) {
      eventPublisher.publish(new FundraiserGoalReached(fundraiserId, fundraiser.raisedCents(), now));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Contribution> listByFundraiser(UUID fundraiserId) {
    Fundraiser fundraiser = fundraiserRepository.findById(fundraiserId).orElseThrow(() -> new FundraiserNotFoundException(fundraiserId));
    if (!fundraiser.ledgerPublic()) {
      throw new LedgerNotPublicException(fundraiserId);
    }
    return contributionRepository.findByFundraiserId(fundraiserId);
  }
}
