package dev.civicpulse.elections.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record RegisterPersonalVoteRequest(@NotBlank String office, @NotBlank String candidateName, String candidatePartyAcronym, UUID politicianAccountId) {}
