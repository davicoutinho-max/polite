package dev.civicpulse.participation.application;

import dev.civicpulse.participation.application.port.in.GetPetitionUseCase;
import dev.civicpulse.participation.application.port.in.ManagePetitionUseCase;
import dev.civicpulse.participation.application.port.in.SignatureVerificationStarted;
import dev.civicpulse.participation.application.port.in.StartSignatureCommand;
import dev.civicpulse.participation.application.port.out.EventPublisher;
import dev.civicpulse.participation.application.port.out.PetitionRepository;
import dev.civicpulse.participation.application.port.out.PetitionSignatureRepository;
import dev.civicpulse.participation.application.port.out.PetitionSignatureVerificationRepository;
import dev.civicpulse.participation.domain.event.PetitionSigned;
import dev.civicpulse.participation.domain.exception.AlreadySignedException;
import dev.civicpulse.participation.domain.exception.InvalidCpfException;
import dev.civicpulse.participation.domain.exception.PetitionNotFoundException;
import dev.civicpulse.participation.domain.exception.VerificationFailedException;
import dev.civicpulse.participation.domain.model.CpfValidator;
import dev.civicpulse.participation.domain.model.PendingSignatureVerification;
import dev.civicpulse.participation.domain.model.Petition;
import dev.civicpulse.participation.domain.model.PetitionSignature;
import dev.civicpulse.participation.domain.model.PetitionType;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetitionService implements ManagePetitionUseCase, GetPetitionUseCase {

  private static final int CODE_VALIDITY_MINUTES = 10;

  private final PetitionRepository petitionRepository;
  private final PetitionSignatureRepository petitionSignatureRepository;
  private final PetitionSignatureVerificationRepository verificationRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;
  private final SecureRandom random = new SecureRandom();

  public PetitionService(
      PetitionRepository petitionRepository,
      PetitionSignatureRepository petitionSignatureRepository,
      PetitionSignatureVerificationRepository verificationRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.petitionRepository = petitionRepository;
    this.petitionSignatureRepository = petitionSignatureRepository;
    this.verificationRepository = verificationRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Petition create(
      String title,
      String summary,
      String category,
      int goal,
      LocalDate deadline,
      String imageUrl,
      String videoUrl,
      String fileUrl,
      String fileName,
      PetitionType petitionType) {
    return petitionRepository.save(
        Petition.create(UUID.randomUUID(), title, summary, category, goal, deadline, imageUrl, videoUrl, fileUrl, fileName, petitionType));
  }

  @Override
  @Transactional
  public SignatureVerificationStarted startSignature(UUID petitionId, UUID citizenAccountId, StartSignatureCommand command) {
    if (petitionSignatureRepository.exists(petitionId, citizenAccountId)) {
      throw new AlreadySignedException();
    }
    petitionRepository.findById(petitionId).orElseThrow(() -> new PetitionNotFoundException(petitionId));
    if (!CpfValidator.isValid(command.cpf())) {
      throw new InvalidCpfException();
    }
    if (command.typedSignature() == null || command.typedSignature().isBlank()) {
      throw new IllegalArgumentException("A typed signature is required");
    }

    Instant now = clock.instant();
    String code = generateCode();
    var pending =
        PendingSignatureVerification.create(
            UUID.randomUUID(),
            petitionId,
            citizenAccountId,
            code,
            command.contact(),
            command.verificationMethod(),
            command.fullName(),
            command.cpf(),
            command.birthDate(),
            command.city(),
            command.state(),
            command.electoralData(),
            command.eSignatureConsent(),
            command.typedSignature(),
            now.plus(CODE_VALIDITY_MINUTES, ChronoUnit.MINUTES));
    verificationRepository.save(pending);

    return new SignatureVerificationStarted(pending.id(), code, command.contact(), command.verificationMethod());
  }

  @Override
  @Transactional
  public void confirmSignature(UUID petitionId, UUID citizenAccountId, UUID verificationId, String code) {
    PendingSignatureVerification pending =
        verificationRepository.findById(verificationId).orElseThrow(() -> new VerificationFailedException("Verification not found"));
    if (!pending.petitionId().equals(petitionId) || !pending.citizenAccountId().equals(citizenAccountId)) {
      throw new VerificationFailedException("Verification does not match this petition/account");
    }
    if (pending.isConsumed()) {
      throw new VerificationFailedException("This verification was already used");
    }
    if (pending.isExpired(clock.instant())) {
      throw new VerificationFailedException("This code has expired — request a new one");
    }
    if (!pending.matches(code)) {
      throw new VerificationFailedException("Incorrect code");
    }
    if (petitionSignatureRepository.exists(petitionId, citizenAccountId)) {
      throw new AlreadySignedException();
    }
    Petition petition = petitionRepository.findById(petitionId).orElseThrow(() -> new PetitionNotFoundException(petitionId));

    Instant now = clock.instant();
    petitionSignatureRepository.save(
        PetitionSignature.sign(
            petitionId,
            citizenAccountId,
            now,
            pending.fullName(),
            pending.cpf(),
            pending.birthDate().orElse(null),
            pending.city().orElse(null),
            pending.state().orElse(null),
            pending.method(),
            pending.electoralData().orElse(null),
            pending.eSignatureConsent(),
            true,
            pending.typedSignature()));
    petition.recordSignature();
    petitionRepository.save(petition);

    pending.consume();
    verificationRepository.save(pending);

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

  private String generateCode() {
    return String.format("%06d", random.nextInt(1_000_000));
  }
}
