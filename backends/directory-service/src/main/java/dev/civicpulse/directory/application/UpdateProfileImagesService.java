package dev.civicpulse.directory.application;

import dev.civicpulse.directory.application.port.in.UpdateProfileImagesUseCase;
import dev.civicpulse.directory.application.port.out.PartyRepository;
import dev.civicpulse.directory.application.port.out.PoliticianRepository;
import dev.civicpulse.directory.domain.exception.PartyNotFoundException;
import dev.civicpulse.directory.domain.exception.PoliticianNotFoundException;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProfileImagesService implements UpdateProfileImagesUseCase {

  private final PoliticianRepository politicianRepository;
  private final PartyRepository partyRepository;
  private final Clock clock;

  public UpdateProfileImagesService(PoliticianRepository politicianRepository, PartyRepository partyRepository, Clock clock) {
    this.politicianRepository = politicianRepository;
    this.partyRepository = partyRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void updatePoliticianProfileImages(UUID accountId, String avatarUrl, String coverImageUrl) {
    var politician = politicianRepository.findById(accountId).orElseThrow(() -> new PoliticianNotFoundException(accountId));
    politician.updateProfileImages(avatarUrl, coverImageUrl, clock.instant());
    politicianRepository.save(politician);
  }

  @Override
  @Transactional
  public void updatePartyLogo(UUID partyId, String logoUrl) {
    var party = partyRepository.findById(partyId).orElseThrow(() -> new PartyNotFoundException(partyId));
    party.updateLogo(logoUrl, clock.instant());
    partyRepository.save(party);
  }
}
