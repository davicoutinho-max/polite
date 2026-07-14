package dev.civicpulse.participation.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreatePetitionRequest(@NotBlank String title, String summary, String category, @Positive int goal, LocalDate deadline) {}
