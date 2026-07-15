package dev.civicpulse.participation.application;

import dev.civicpulse.participation.application.port.in.GetPetitionUseCase;
import dev.civicpulse.participation.application.port.in.ManagePetitionUseCase;
import dev.civicpulse.participation.application.port.out.EventPublisher;
import dev.civicpulse.participation.application.port.out.PetitionRepository;
import dev.civicpulse.participation.application.port.out.PetitionSignatureRepository;
import dev.civicpulse.participation.domain.event.PetitionSigned;
import dev.civicpulse.participation.domain.exception.AlreadySignedException;
import dev.civicpulse.participation.domain.exception.PetitionNotFoundException;
import dev.civicpulse.participation.domain.model.Petition;
import dev.civicpulse.participation.domain.model.PetitionSignature;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetitionService implements ManagePetitionUseCase, GetPetitionUseCase {

  private final PetitionRepository petitionRepository;
  private final PetitionSignatureRepository petitionSignatureRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public PetitionService(
      PetitionRepository petitionRepository, PetitionSignatureRepository petitionSignatureRepository, EventPublisher eventPublisher, Clock clock) {
    this.petitionRepository = petitionRepository;
    this.petitionSignatureRepository = petitionSignatureRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Petition create(String title, String summary, String category, int goal, LocalDate deadline) {
    return petitionRepository.save(Petition.create(UUID.randomUUID(), title, summary, category, goal, deadline));
  }

  @Override
  @Transactional
  public void sign(UUID petitionId, UUID citizenAccountId) {
    if (petitionSignatureRepository.exists(petitionId, citizenAccountId)) {
      throw new AlreadySignedException();
    }
    Petition petition = petitionRepository.findById(petitionId).orElseThrow(() -> new PetitionNotFoundException(petitionId));

    Instant now = clock.instant();
    petitionSignatureRepository.save(PetitionSignature.sign(petitionId, citizenAccountId, now));
    petition.recordSignature();
    petitionRepository.save(petition);

    eventPublisher.publish(new PetitionSigned(petitionId, citizenAccountId, now));
  }

  @Override
  @Transactional(readOnly = true)
  public Petition getById(UUID id) {
    return petitionRepository.findById(id).orElseThrow(() -> new PetitionNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Petition> list(int page, int pageSize) {
    return petitionRepository.findAll(page, pageSize);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasSigned(UUID petitionId, UUID citizenAccountId) {
    return petitionSignatureRepository.exists(petitionId, citizenAccountId);
  }
}
