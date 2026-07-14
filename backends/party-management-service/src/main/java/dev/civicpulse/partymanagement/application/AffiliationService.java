package dev.civicpulse.partymanagement.application;

import dev.civicpulse.partymanagement.application.port.in.ReviewAffiliationRequestUseCase;
import dev.civicpulse.partymanagement.application.port.out.AffiliationRequestRepository;
import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.PartyMemberRepository;
import dev.civicpulse.partymanagement.domain.event.AffiliationRequestApproved;
import dev.civicpulse.partymanagement.domain.event.AffiliationRequestRejected;
import dev.civicpulse.partymanagement.domain.exception.AffiliationRequestNotFoundException;
import dev.civicpulse.partymanagement.domain.model.AffiliationRequest;
import dev.civicpulse.partymanagement.domain.model.AffiliationRequestStatus;
import dev.civicpulse.partymanagement.domain.model.PartyMember;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AffiliationService implements ReviewAffiliationRequestUseCase {

  private final AffiliationRequestRepository requestRepository;
  private final PartyMemberRepository memberRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public AffiliationService(
      AffiliationRequestRepository requestRepository, PartyMemberRepository memberRepository, EventPublisher eventPublisher, Clock clock) {
    this.requestRepository = requestRepository;
    this.memberRepository = memberRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void onAffiliationRequested(UUID requestId, UUID partyId, UUID citizenAccountId, String city, Instant requestedAt) {
    if (requestRepository.findById(requestId).isPresent()) {
      return;
    }
    requestRepository.save(AffiliationRequest.create(requestId, partyId, citizenAccountId, city, requestedAt));
  }

  @Override
  @Transactional
  public AffiliationRequest approve(UUID requestId) {
    AffiliationRequest request = requestRepository.findById(requestId).orElseThrow(() -> new AffiliationRequestNotFoundException(requestId));
    Instant now = clock.instant();
    request.approve(now);
    AffiliationRequest saved = requestRepository.save(request);

    memberRepository.save(PartyMember.admit(UUID.randomUUID(), request.partyId(), request.citizenAccountId(), request.city().orElse(null), now));

    eventPublisher.publish(new AffiliationRequestApproved(requestId, request.partyId(), request.citizenAccountId(), now));
    return saved;
  }

  @Override
  @Transactional
  public AffiliationRequest reject(UUID requestId) {
    AffiliationRequest request = requestRepository.findById(requestId).orElseThrow(() -> new AffiliationRequestNotFoundException(requestId));
    Instant now = clock.instant();
    request.reject(now);
    AffiliationRequest saved = requestRepository.save(request);

    eventPublisher.publish(new AffiliationRequestRejected(requestId, request.partyId(), request.citizenAccountId(), now));
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public List<AffiliationRequest> listPending(UUID partyId) {
    return requestRepository.findByPartyIdAndStatus(partyId, AffiliationRequestStatus.PENDING);
  }
}
