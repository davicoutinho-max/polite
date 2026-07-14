package dev.civicpulse.participation.application;

import dev.civicpulse.participation.application.port.in.GetConsultationUseCase;
import dev.civicpulse.participation.application.port.in.ManageConsultationUseCase;
import dev.civicpulse.participation.application.port.out.ConsultationRepository;
import dev.civicpulse.participation.application.port.out.ConsultationResponseRepository;
import dev.civicpulse.participation.application.port.out.EventPublisher;
import dev.civicpulse.participation.domain.event.ConsultationStanceSet;
import dev.civicpulse.participation.domain.exception.ConsultationNotFoundException;
import dev.civicpulse.participation.domain.model.Consultation;
import dev.civicpulse.participation.domain.model.ConsultationResponse;
import dev.civicpulse.participation.domain.model.ConsultationStance;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultationService implements ManageConsultationUseCase, GetConsultationUseCase {

  private final ConsultationRepository consultationRepository;
  private final ConsultationResponseRepository consultationResponseRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public ConsultationService(
      ConsultationRepository consultationRepository,
      ConsultationResponseRepository consultationResponseRepository,
      EventPublisher eventPublisher,
      Clock clock) {
    this.consultationRepository = consultationRepository;
    this.consultationResponseRepository = consultationResponseRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Consultation create(String title, String description, LocalDate deadline) {
    return consultationRepository.save(Consultation.create(UUID.randomUUID(), title, description, deadline));
  }

  @Override
  @Transactional
  public void respond(UUID consultationId, UUID citizenAccountId, ConsultationStance stance) {
    Consultation consultation =
        consultationRepository.findById(consultationId).orElseThrow(() -> new ConsultationNotFoundException(consultationId));
    Instant now = clock.instant();

    var existing = consultationResponseRepository.findByConsultationAndCitizen(consultationId, citizenAccountId);
    if (existing.isPresent()) {
      existing.get().changeStance(stance, now);
      consultationResponseRepository.save(existing.get());
    } else {
      consultationResponseRepository.save(ConsultationResponse.respond(consultationId, citizenAccountId, stance, now));
      consultation.recordNewResponse();
      consultationRepository.save(consultation);
    }

    eventPublisher.publish(new ConsultationStanceSet(consultationId, citizenAccountId, stance.code(), now));
  }

  @Override
  @Transactional(readOnly = true)
  public Consultation getById(UUID id) {
    return consultationRepository.findById(id).orElseThrow(() -> new ConsultationNotFoundException(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Consultation> list(int page, int pageSize) {
    return consultationRepository.findAll(page, pageSize);
  }
}
