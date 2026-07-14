package dev.civicpulse.partymanagement.application;

import dev.civicpulse.partymanagement.application.port.in.ManagePartyProfileUseCase;
import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.PartyProfileRepository;
import dev.civicpulse.partymanagement.domain.event.PartyProfileUpdated;
import dev.civicpulse.partymanagement.domain.exception.PartyProfileNotFoundException;
import dev.civicpulse.partymanagement.domain.model.PartyProfile;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartyProfileService implements ManagePartyProfileUseCase {

  private final PartyProfileRepository profileRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public PartyProfileService(PartyProfileRepository profileRepository, EventPublisher eventPublisher, Clock clock) {
    this.profileRepository = profileRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void onPartyRegistered(UUID partyId) {
    if (profileRepository.existsByPartyId(partyId)) {
      return;
    }
    profileRepository.save(PartyProfile.createBlank(partyId, clock.instant()));
  }

  @Override
  @Transactional(readOnly = true)
  public PartyProfile getProfile(UUID partyId) {
    return profileRepository.findByPartyId(partyId).orElseThrow(() -> new PartyProfileNotFoundException(partyId));
  }

  @Override
  @Transactional
  public PartyProfile updateProfile(UUID partyId, String history, String program, String statuteUrl, String coverUrl) {
    PartyProfile profile = profileRepository.findByPartyId(partyId).orElseThrow(() -> new PartyProfileNotFoundException(partyId));
    profile.update(history, program, statuteUrl, coverUrl, clock.instant());
    PartyProfile saved = profileRepository.save(profile);
    eventPublisher.publish(new PartyProfileUpdated(partyId, saved.updatedAt()));
    return saved;
  }
}
