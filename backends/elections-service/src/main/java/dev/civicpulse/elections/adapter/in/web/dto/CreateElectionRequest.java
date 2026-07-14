package dev.civicpulse.elections.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateElectionRequest(@NotBlank String title, @NotBlank String scope, @NotNull LocalDate electionDate, String description) {}
