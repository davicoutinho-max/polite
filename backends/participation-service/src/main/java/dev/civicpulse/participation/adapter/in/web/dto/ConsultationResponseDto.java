package dev.civicpulse.participation.adapter.in.web.dto;

import dev.civicpulse.participation.domain.model.Consultation;
import java.time.LocalDate;
import java.util.UUID;

public record ConsultationResponseDto(UUID id, String title, String description, LocalDate deadline, int responsesCount) {

  public static ConsultationResponseDto from(Consultation consultation) {
    return new ConsultationResponseDto(
        consultation.id(), consultation.title(), consultation.description().orElse(null), consultation.deadline().orElse(null),
        consultation.responsesCount());
  }
}
