package dev.civicpulse.participation.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateSurveyRequest(@NotBlank String question, String context, @NotEmpty List<String> options) {}
