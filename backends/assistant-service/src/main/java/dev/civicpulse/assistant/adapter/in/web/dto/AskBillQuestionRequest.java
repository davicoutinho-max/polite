package dev.civicpulse.assistant.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** {@code question} is capped well below what the model itself would accept — the length limit
 * is a deliberate abuse guardrail (see GeminiApiClient's javadoc), not a model constraint. */
public record AskBillQuestionRequest(
    @NotBlank @Size(max = 300) String billIdentification,
    @NotBlank @Size(max = 2000) String billSummary,
    @NotBlank @Size(max = 400) String question) {}
