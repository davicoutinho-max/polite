package dev.civicpulse.membershipaffiliation.application;

import dev.civicpulse.membershipaffiliation.application.port.in.ManageAffiliationUseCase;
import dev.civicpulse.membershipaffiliation.application.port.out.AffiliationRepository;
import dev.civicpulse.membershipaffiliation.application.port.out.AffiliationStatusHistoryRepository;
import dev.civicpulse.membershipaffiliation.application.port.out.EventPublisher;
import dev.civicpulse.membershipaffiliation.application.port.out.MembershipCardRepository;
import dev.civicpulse.membershipaffiliation.domain.event.AffiliationConfirmed;
import dev.civicpulse.membershipaffiliation.domain.event.AffiliationRequested;
import dev.civicpulse.membershipaffiliation.domain.event.AffiliationUnderReview;
import dev.civicpulse.membershipaffiliation.domain.event.MembershipCardIssued;
import dev.civicpulse.membershipaffiliation.domain.exception.ActiveAffiliationAlreadyExistsException;
import dev.civicpulse.membershipaffiliation.domain.exception.AffiliationNotFoundException;
import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatus;
import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatusHistoryEntry;
import dev.civicpulse.membershipaffiliation.domain.model.ChangedBy;
import dev.civicpulse.membershipaffiliation.domain.model.MembershipCard;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AffiliationService implements ManageAffiliationUseCase {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final AffiliationRepository affiliationRepository;
  private final AffiliationStatusHistoryRepository historyRepository;
  private final MembershipCardRepository membershipCardRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public AffiliationService(
      AffiliationRepository affiliationRepository,
      AffiliationStatusHistoryRepository historyRepository,
      MembershipCardRepository membershipCardRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.affiliationRepository = affiliationRepository;
    this.historyRepository = historyRepository;
    this.membershipCardRepository = membershipCardRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Affiliation requestAffiliation(UUID citizenAccountId, UUID partyId, String city) {
    if (affiliationRepository.existsActiveByCitizenAndParty(citizenAccountId, partyId)) {
      throw new ActiveAffiliationAlreadyExistsException();
    }
    Instant now = clock.instant();
    UUID affiliationId = UUID.randomUUID();
    Affiliation affiliation = Affiliation.request(affiliationId, citizenAccountId, partyId, now);
    Affiliation saved = affiliationRepository.save(affiliation);
    recordHistory(affiliationId, null, saved.status(), ChangedBy.CITIZEN, now);
    eventPublisher.publish(new AffiliationRequested(affiliationId, partyId, citizenAccountId, city, now));

    // Immediately enters the party's review queue — there is no distinct "start review" trigger
    // from another service in this design (Party Management's own review row is created by
    // consuming AffiliationRequested itself), so this transition is automatic.
    saved.startReview(now);
    Affiliation reviewing = affiliationRepository.save(saved);
    recordHistory(affiliationId, AffiliationStatus.REQUESTED, reviewing.status(), ChangedBy.SYSTEM, now);
    eventPublisher.publish(new AffiliationUnderReview(affiliationId, now));

    return reviewing;
  }

  @Override
  @Transactional
  public void onAffiliationRequestApproved(UUID affiliationId) {
    Affiliation affiliation = affiliationRepository.findById(affiliationId).orElseThrow(() -> new AffiliationNotFoundException(affiliationId));
    var from = affiliation.status();
    Instant now = clock.instant();
    affiliation.approveByParty(now);
    affiliationRepository.save(affiliation);
    recordHistory(affiliationId, from, affiliation.status(), ChangedBy.PARTY, now);
  }

  @Override
  @Transactional
  public void onAffiliationRequestRejected(UUID affiliationId) {
    Affiliation affiliation = affiliationRepository.findById(affiliationId).orElseThrow(() -> new AffiliationNotFoundException(affiliationId));
    var from = affiliation.status();
    Instant now = clock.instant();
    affiliation.reject(now);
    affiliationRepository.save(affiliation);
    recordHistory(affiliationId, from, affiliation.status(), ChangedBy.PARTY, now);
  }

  @Override
  @Transactional
  public Affiliation sendToElectoralJustice(UUID affiliationId) {
    Affiliation affiliation = affiliationRepository.findById(affiliationId).orElseThrow(() -> new AffiliationNotFoundException(affiliationId));
    var from = affiliation.status();
    Instant now = clock.instant();
    affiliation.sendToElectoralJustice(now);
    Affiliation saved = affiliationRepository.save(affiliation);
    recordHistory(affiliationId, from, saved.status(), ChangedBy.SYSTEM, now);
    return saved;
  }

  @Override
  @Transactional
  public Affiliation confirmAffiliation(UUID affiliationId) {
    Affiliation affiliation = affiliationRepository.findById(affiliationId).orElseThrow(() -> new AffiliationNotFoundException(affiliationId));
    var from = affiliation.status();
    Instant now = clock.instant();
    affiliation.confirm(now);
    Affiliation saved = affiliationRepository.save(affiliation);
    recordHistory(affiliationId, from, saved.status(), ChangedBy.ELECTORAL_JUSTICE, now);
    eventPublisher.publish(new AffiliationConfirmed(affiliationId, saved.citizenAccountId(), saved.partyId(), now));

    String memberNumber = generateUniqueMemberNumber();
    String qrPayload = "https://civicpulse.dev/verify/" + affiliationId;
    membershipCardRepository.save(MembershipCard.issue(affiliationId, memberNumber, qrPayload, now));
    eventPublisher.publish(new MembershipCardIssued(affiliationId, memberNumber, now));

    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public Affiliation getById(UUID id) {
    return affiliationRepository.findById(id).orElseThrow(() -> new AffiliationNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Affiliation> listByCitizen(UUID citizenAccountId) {
    return affiliationRepository.findByCitizenAccountId(citizenAccountId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MembershipCard> getCard(UUID affiliationId) {
    return membershipCardRepository.findByAffiliationId(affiliationId);
  }

  private void recordHistory(UUID affiliationId, AffiliationStatus from, AffiliationStatus to, ChangedBy changedBy, Instant now) {
    historyRepository.save(AffiliationStatusHistoryEntry.record(UUID.randomUUID(), affiliationId, from, to, changedBy, now));
  }

  private String generateUniqueMemberNumber() {
    String candidate;
    do {
      candidate = "CP-" + (100000 + RANDOM.nextInt(900000));
    } while (membershipCardRepository.existsByMemberNumber(candidate));
    return candidate;
  }
}
