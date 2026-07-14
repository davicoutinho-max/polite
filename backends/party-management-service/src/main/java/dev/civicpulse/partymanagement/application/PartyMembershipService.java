package dev.civicpulse.partymanagement.application;

import dev.civicpulse.partymanagement.application.port.in.ManagePartyMembershipUseCase;
import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.PartyMemberRepository;
import dev.civicpulse.partymanagement.domain.event.PartyMemberStatusChanged;
import dev.civicpulse.partymanagement.domain.exception.PartyMemberNotFoundException;
import dev.civicpulse.partymanagement.domain.model.PartyMember;
import dev.civicpulse.partymanagement.domain.model.PartyMemberStatus;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartyMembershipService implements ManagePartyMembershipUseCase {

  private final PartyMemberRepository memberRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public PartyMembershipService(PartyMemberRepository memberRepository, EventPublisher eventPublisher, Clock clock) {
    this.memberRepository = memberRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public PartyMember changeStatus(UUID partyId, UUID citizenAccountId, PartyMemberStatus newStatus) {
    PartyMember member =
        memberRepository
            .findByPartyIdAndCitizenAccountId(partyId, citizenAccountId)
            .orElseThrow(() -> new PartyMemberNotFoundException(partyId, citizenAccountId));
    member.changeStatus(newStatus);
    PartyMember saved = memberRepository.save(member);
    eventPublisher.publish(new PartyMemberStatusChanged(partyId, citizenAccountId, newStatus.code(), clock.instant()));
    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public List<PartyMember> listByParty(UUID partyId) {
    return memberRepository.findByPartyId(partyId);
  }
}
