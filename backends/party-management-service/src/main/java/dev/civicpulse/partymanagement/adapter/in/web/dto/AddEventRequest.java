package dev.civicpulse.partymanagement.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AddEventRequest(
    @NotBlank String title, @NotNull LocalDate eventDate, String location, String tagLabel, String tagSeverity) {}
