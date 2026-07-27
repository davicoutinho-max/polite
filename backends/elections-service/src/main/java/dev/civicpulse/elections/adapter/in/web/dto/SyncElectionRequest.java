package dev.civicpulse.elections.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Internal-only, called by government-sync-service — see ManageElectionUseCase.syncElection. */
public record SyncElectionRequest(
    @NotBlank String title, @NotBlank String scope, @NotNull LocalDate electionDate, String location, String description) {}
