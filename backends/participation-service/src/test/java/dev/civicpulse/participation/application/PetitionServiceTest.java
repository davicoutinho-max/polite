package dev.civicpulse.participation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import dev.civicpulse.participation.domain.model.PendingSignatureVerification;
import dev.civicpulse.participation.domain.model.Petition;
import dev.civicpulse.participation.domain.model.PetitionType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PetitionServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
  private static final String VALID_CPF = "529.982.247-25";

  @Mock private PetitionRepository petitionRepository;
  @Mock private PetitionSignatureRepository petitionSignatureRepository;
  @Mock private PetitionSignatureVerificationRepository verificationRepository;
  @Mock private EventPublisher eventPublisher;

  private PetitionService service;

  @BeforeEach
  void setUp() {
    service =
        new PetitionService(
            petitionRepository, petitionSignatureRepository, verificationRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createSavesPetition() {
    when(petitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Petition petition = service.create("title", "summary", "category", 500, null, null, null, null, null, PetitionType.VERIFIED_SUPPORT);

    assertThat(petition.title()).isEqualTo("title");
  }

  @Test
  void startSignatureThrowsWhenAlreadySigned() {
    UUID petitionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    when(petitionSignatureRepository.exists(petitionId, citizenId)).thenReturn(true);

    assertThatThrownBy(() -> service.startSignature(petitionId, citizenId, command()))
        .isInstanceOf(AlreadySignedException.class);
    verify(petitionRepository, never()).findById(any());
  }

  @Test
  void startSignatureThrowsWhenPetitionMissing() {
    UUID petitionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    when(petitionSignatureRepository.exists(petitionId, citizenId)).thenReturn(false);
    when(petitionRepository.findById(petitionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.startSignature(petitionId, citizenId, command())).isInstanceOf(PetitionNotFoundException.class);
  }

  @Test
  void startSignatureThrowsOnInvalidCpf() {
    UUID petitionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    when(petitionSignatureRepository.exists(petitionId, citizenId)).thenReturn(false);
    when(petitionRepository.findById(petitionId))
        .thenReturn(Optional.of(Petition.create(petitionId, "title", null, null, 100, null, null, null, null, null, PetitionType.VERIFIED_SUPPORT)));

    var invalidCommand =
        new StartSignatureCommand("Jane Doe", "111.111.111-11", null, "City", "ST", "sms", "+5511999990000", null, true, "Jane Doe");

    assertThatThrownBy(() -> service.startSignature(petitionId, citizenId, invalidCommand)).isInstanceOf(InvalidCpfException.class);
  }

  @Test
  void startSignaturePersistsPendingVerification() {
    UUID petitionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    when(petitionSignatureRepository.exists(petitionId, citizenId)).thenReturn(false);
    when(petitionRepository.findById(petitionId))
        .thenReturn(Optional.of(Petition.create(petitionId, "title", null, null, 100, null, null, null, null, null, PetitionType.VERIFIED_SUPPORT)));
    when(verificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var started = service.startSignature(petitionId, citizenId, command());

    assertThat(started.demoCode()).hasSize(6);
    assertThat(started.contact()).isEqualTo("+5511999990000");
    verify(verificationRepository).save(any(PendingSignatureVerification.class));
  }

  @Test
  void confirmSignatureThrowsWhenCodeWrong() {
    UUID petitionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    UUID verificationId = UUID.randomUUID();
    var pending = pendingVerification(verificationId, petitionId, citizenId, "123456");
    when(verificationRepository.findById(verificationId)).thenReturn(Optional.of(pending));

    assertThatThrownBy(() -> service.confirmSignature(petitionId, citizenId, verificationId, "000000"))
        .isInstanceOf(VerificationFailedException.class);
  }

  @Test
  void confirmSignatureIncrementsSignaturesAndPublishesEvent() {
    UUID petitionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    UUID verificationId = UUID.randomUUID();
    var pending = pendingVerification(verificationId, petitionId, citizenId, "123456");
    Petition petition = Petition.create(petitionId, "title", null, null, 100, null, null, null, null, null, PetitionType.VERIFIED_SUPPORT);
    when(verificationRepository.findById(verificationId)).thenReturn(Optional.of(pending));
    when(petitionSignatureRepository.exists(petitionId, citizenId)).thenReturn(false);
    when(petitionRepository.findById(petitionId)).thenReturn(Optional.of(petition));

    service.confirmSignature(petitionId, citizenId, verificationId, "123456");

    assertThat(petition.signaturesCount()).isEqualTo(1);
    assertThat(pending.isConsumed()).isTrue();
    verify(petitionSignatureRepository).save(any());
    verify(eventPublisher).publish(any(PetitionSigned.class));
  }

  private static StartSignatureCommand command() {
    return new StartSignatureCommand("Jane Doe", VALID_CPF, null, "City", "ST", "sms", "+5511999990000", null, true, "Jane Doe");
  }

  private static PendingSignatureVerification pendingVerification(UUID id, UUID petitionId, UUID citizenId, String code) {
    return PendingSignatureVerification.create(
        id, petitionId, citizenId, code, "+5511999990000", "sms", "Jane Doe", VALID_CPF, null, "City", "ST", null, true, "Jane Doe",
        NOW.plusSeconds(600));
  }
}
