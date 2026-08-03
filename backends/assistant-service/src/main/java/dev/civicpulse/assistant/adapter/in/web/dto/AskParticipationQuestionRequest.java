package dev.civicpulse.assistant.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** {@code question} is capped well below what the model itself would accept — the length limit
 * is a deliberate abuse guardrail (see GeminiApiClient's javadoc) against free-form user input,
 * not a model constraint. {@code description} is real petition/consultation/survey content the
 * citizen never typed here (it's already stored elsewhere) — capped much higher, just to keep a
 * single request bounded, not to fit a "typical" description. */
public record AskParticipationQuestionRequest(
    @NotBlank @Size(max = 30) String itemType,
    @NotBlank @Size(max = 300) String title,
    @NotBlank @Size(max = 20000) String description,
    @NotBlank @Size(max = 400) String question) {}
