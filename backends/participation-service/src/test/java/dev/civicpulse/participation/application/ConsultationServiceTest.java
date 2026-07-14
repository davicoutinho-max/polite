package dev.civicpulse.participation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.participation.application.port.out.ConsultationRepository;
import dev.civicpulse.participation.application.port.out.ConsultationResponseRepository;
import dev.civicpulse.participation.application.port.out.EventPublisher;
import dev.civicpulse.participation.domain.exception.ConsultationNotFoundException;
import dev.civicpulse.participation.domain.model.Consultation;
import dev.civicpulse.participation.domain.model.ConsultationResponse;
import dev.civicpulse.participation.domain.model.ConsultationStance;
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
class ConsultationServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private ConsultationRepository consultationRepository;
  @Mock private ConsultationResponseRepository consultationResponseRepository;
  @Mock private EventPublisher eventPublisher;

  private ConsultationService service;

  @BeforeEach
  void setUp() {
    service = new ConsultationService(consultationRepository, consultationResponseRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void respondThrowsWhenConsultationMissing() {
    UUID consultationId = UUID.randomUUID();
    when(consultationRepository.findById(consultationId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.respond(consultationId, UUID.randomUUID(), ConsultationStance.FAVOR))
        .isInstanceOf(ConsultationNotFoundException.class);
  }

  @Test
  void firstResponseIncrementsCountAndPublishesEvent() {
    UUID consultationId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    Consultation consultation = Consultation.create(consultationId, "title", null, null);
    when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultation));
    when(consultationResponseRepository.findByConsultationAndCitizen(consultationId, citizenId)).thenReturn(Optional.empty());

    service.respond(consultationId, citizenId, ConsultationStance.FAVOR);

    assertThat(consultation.responsesCount()).isEqualTo(1);
    verify(consultationResponseRepository).save(any());
    verify(eventPublisher).publish(any());
  }

  @Test
  void secondResponseChangesStanceWithoutDoubleCounting() {
    UUID consultationId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    Consultation consultation = Consultation.create(consultationId, "title", null, null);
    consultation.recordNewResponse();
    ConsultationResponse existing = ConsultationResponse.respond(consultationId, citizenId, ConsultationStance.FAVOR, NOW);
    when(consultationRepository.findById(consultationId)).thenReturn(Optional.of(consultation));
    when(consultationResponseRepository.findByConsultationAndCitizen(consultationId, citizenId)).thenReturn(Optional.of(existing));

    service.respond(consultationId, citizenId, ConsultationStance.AGAINST);

    assertThat(consultation.responsesCount()).isEqualTo(1);
    assertThat(existing.stance()).isEqualTo(ConsultationStance.AGAINST);
    verify(consultationRepository, org.mockito.Mockito.never()).save(any());
  }
}
