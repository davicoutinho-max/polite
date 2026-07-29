package dev.civicpulse.assistant.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** {@code question} is capped well below what the model itself would accept — the length limit
 * is a deliberate abuse guardrail (see GeminiApiClient's javadoc), not a model constraint. */
public record AskParticipationQuestionRequest(
    @NotBlank @Size(max = 30) String itemType,
    @NotBlank @Size(max = 300) String title,
    @NotBlank @Size(max = 2000) String description,
    @NotBlank @Size(max = 400) String question) {}
