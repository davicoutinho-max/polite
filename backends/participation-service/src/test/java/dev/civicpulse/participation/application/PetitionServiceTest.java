package dev.civicpulse.participation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.participation.application.port.out.EventPublisher;
import dev.civicpulse.participation.application.port.out.PetitionRepository;
import dev.civicpulse.participation.application.port.out.PetitionSignatureRepository;
import dev.civicpulse.participation.domain.event.PetitionSigned;
import dev.civicpulse.participation.domain.exception.AlreadySignedException;
import dev.civicpulse.participation.domain.exception.PetitionNotFoundException;
import dev.civicpulse.participation.domain.model.Petition;
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

  @Mock private PetitionRepository petitionRepository;
  @Mock private PetitionSignatureRepository petitionSignatureRepository;
  @Mock private EventPublisher eventPublisher;

  private PetitionService service;

  @BeforeEach
  void setUp() {
    service = new PetitionService(petitionRepository, petitionSignatureRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createSavesPetition() {
    when(petitionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Petition petition = service.create("title", "summary", "category", 500, null);

    assertThat(petition.title()).isEqualTo("title");
  }

  @Test
  void signThrowsWhenAlreadySigned() {
    UUID petitionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    when(petitionSignatureRepository.exists(petitionId, citizenId)).thenReturn(true);

    assertThatThrownBy(() -> service.sign(petitionId, citizenId)).isInstanceOf(AlreadySignedException.class);
    verify(petitionRepository, never()).findById(any());
  }

  @Test
  void signThrowsWhenPetitionMissing() {
    UUID petitionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    when(petitionSignatureRepository.exists(petitionId, citizenId)).thenReturn(false);
    when(petitionRepository.findById(petitionId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.sign(petitionId, citizenId)).isInstanceOf(PetitionNotFoundException.class);
  }

  @Test
  void signIncrementsSignaturesAndPublishesEvent() {
    UUID petitionId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    Petition petition = Petition.create(petitionId, "title", null, null, 100, null);
    when(petitionSignatureRepository.exists(petitionId, citizenId)).thenReturn(false);
    when(petitionRepository.findById(petitionId)).thenReturn(Optional.of(petition));

    service.sign(petitionId, citizenId);

    assertThat(petition.signaturesCount()).isEqualTo(1);
    verify(petitionSignatureRepository).save(any());
    verify(eventPublisher).publish(any(PetitionSigned.class));
  }
}
