package dev.civicpulse.fundraising.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateFundraiserRequest(
    @NotBlank String title, String description, @NotBlank String category, @Positive long goalCents, LocalDate deadline, Boolean ledgerPublic) {}
