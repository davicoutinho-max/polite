package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CastVoteRequest(UUID legislativeItemId, @NotBlank String matter, @NotNull LocalDate voteDate, @NotBlank String choice) {}
