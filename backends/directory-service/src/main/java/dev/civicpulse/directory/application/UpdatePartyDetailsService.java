package dev.civicpulse.directory.application;

import dev.civicpulse.directory.application.port.in.UpdatePartyDetailsUseCase;
import dev.civicpulse.directory.application.port.out.PartyRepository;
import dev.civicpulse.directory.domain.exception.PartyNotFoundException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdatePartyDetailsService implements UpdatePartyDetailsUseCase {

  private final PartyRepository partyRepository;
  private final Clock clock;

  public UpdatePartyDetailsService(PartyRepository partyRepository, Clock clock) {
    this.partyRepository = partyRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void updatePartyDetails(
      UUID partyId, String name, String acronym, int number, String ideology, Integer foundedYear, String president) {
    var party = partyRepository.findById(partyId).orElseThrow(() -> new PartyNotFoundException(partyId));
    party.updateDetails(name, acronym, number, ideology, foundedYear, president, clock.instant());
    partyRepository.save(party);
  }
}
