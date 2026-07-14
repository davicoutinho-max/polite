package dev.civicpulse.assistant.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record WriteAnswerRequest(@NotBlank String promptKind, @NotBlank String answerText) {}
