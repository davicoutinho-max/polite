package dev.civicpulse.participation.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateConsultationRequest(@NotBlank String title, String description, LocalDate deadline) {}
