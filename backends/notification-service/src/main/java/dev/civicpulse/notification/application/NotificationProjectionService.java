package dev.civicpulse.notification.application;

import dev.civicpulse.notification.application.port.in.IngestNotificationUseCase;
import dev.civicpulse.notification.application.port.out.AffiliationLookupGateway;
import dev.civicpulse.notification.application.port.out.FundraiserLookupGateway;
import dev.civicpulse.notification.domain.model.NotificationCategory;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Translates upstream domain events (see NotificationServiceApplication's scope note on which
 * ones are wired in this pass) into {@link IngestNotificationUseCase} calls, resolving whichever
 * recipient account id isn't already present on the event via a real synchronous lookup. */
@Service
public class NotificationProjectionService {

  private static final Logger log = LoggerFactory.getLogger(NotificationProjectionService.class);

  private final IngestNotificationUseCase ingestNotificationUseCase;
  private final AffiliationLookupGateway affiliationLookupGateway;
  private final FundraiserLookupGateway fundraiserLookupGateway;

  public NotificationProjectionService(
      IngestNotificationUseCase ingestNotificationUseCase,
      AffiliationLookupGateway affiliationLookupGateway,
      FundraiserLookupGateway fundraiserLookupGateway) {
    this.ingestNotificationUseCase = ingestNotificationUseCase;
    this.affiliationLookupGateway = affiliationLookupGateway;
    this.fundraiserLookupGateway = fundraiserLookupGateway;
  }

  public void onAffiliationConfirmed(UUID affiliationId, UUID citizenAccountId, UUID partyId) {
    ingestNotificationUseCase.ingest(
        citizenAccountId,
        NotificationCategory.PARTY,
        "check_circle",
        "Affiliation confirmed",
        "Your affiliation request has been confirmed.",
        "/membership/" + affiliationId,
        "affiliation-confirmed:" + affiliationId);
  }

  public void onMembershipFeeGenerated(UUID feeId, UUID affiliationId, long amountCents) {
    affiliationLookupGateway
        .lookupCitizenAccountId(affiliationId)
        .ifPresentOrElse(
            citizenAccountId ->
                ingestNotificationUseCase.ingest(
                    citizenAccountId,
                    NotificationCategory.PARTY,
                    "receipt_long",
                    "Membership fee due",
                    "A new membership fee of " + centsToDisplay(amountCents) + " is due.",
                    "/membership/fees/" + feeId,
                    "membership-fee-generated:" + feeId),
            () -> log.debug("Skipping membership-fee-generated for feeId {} — affiliation {} not found", feeId, affiliationId));
  }

  public void onContributionReceived(UUID fundraiserId, UUID contributionId, UUID supporterAccountId, long amountCents) {
    ingestNotificationUseCase.ingest(
        supporterAccountId,
        NotificationCategory.CAMPAIGN,
        "volunteer_activism",
        "Contribution received",
        "Your contribution of " + centsToDisplay(amountCents) + " was received. Thank you!",
        "/fundraising/" + fundraiserId,
        "contribution-received:" + contributionId);
  }

  public void onFundraiserGoalReached(UUID fundraiserId, long raisedCents) {
    fundraiserLookupGateway
        .lookupOrganizerAccountId(fundraiserId)
        .ifPresentOrElse(
            organizerAccountId ->
                ingestNotificationUseCase.ingest(
                    organizerAccountId,
                    NotificationCategory.CAMPAIGN,
                    "flag_circle",
                    "Fundraising goal reached",
                    "Your fundraiser reached its goal — " + centsToDisplay(raisedCents) + " raised so far.",
                    "/fundraising/" + fundraiserId,
                    "fundraiser-goal-reached:" + fundraiserId),
            () -> log.debug("Skipping fundraiser-goal-reached for fundraiserId {} — fundraiser not found", fundraiserId));
  }

  private static String centsToDisplay(long amountCents) {
    return String.format("R$ %.2f", amountCents / 100.0);
  }
}
